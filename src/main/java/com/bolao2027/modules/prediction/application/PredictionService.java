package com.bolao2027.modules.prediction.application;

import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.domain.PredictionLockedException;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço de aplicação para gerenciamento de palpites.
 * Controla o fluxo de criação, atualização e consulta de palpites,
 * respeitando o bloqueio de 1 hora antes do início da partida (HU-01, HU-03).
 */
@Service
@Transactional
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public PredictionService(PredictionRepository predictionRepository,
                             MatchRepository matchRepository,
                             UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    /**
     * Salva ou atualiza o palpite de um participante para uma partida.
     * Lança {@link PredictionLockedException} se a janela de palpites já foi encerrada.
     *
     * @param userId    ID do participante
     * @param matchId   ID da partida
     * @param homeScore Placar palpitado do mandante (≥ 0)
     * @param awayScore Placar palpitado do visitante (≥ 0)
     * @return O palpite salvo ou atualizado
     */
    public Prediction savePrediction(Long userId, Long matchId, int homeScore, int awayScore) {
        if (homeScore < 0 || awayScore < 0) {
            throw new IllegalArgumentException("Placares não podem ser negativos.");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + matchId));

        if (match.isLocked()) {
            throw new PredictionLockedException(
                    "A janela de palpites para esta partida foi encerrada. Bloqueio a T-1h antes do início.");
        }

        Optional<Prediction> existing = predictionRepository.findByUserIdAndMatchId(userId, matchId);
        if (existing.isPresent()) {
            Prediction p = existing.get();
            p.updateScore(homeScore, awayScore);
            return predictionRepository.save(p);
        } else {
            Prediction p = new Prediction(userId, matchId, homeScore, awayScore);
            return predictionRepository.save(p);
        }
    }

    /**
     * Retorna o palpite existente de um participante para uma partida, ou empty se não houver.
     */
    @Transactional(readOnly = true)
    public Optional<Prediction> getOrCreatePrediction(Long userId, Long matchId) {
        return predictionRepository.findByUserIdAndMatchId(userId, matchId);
    }

    /**
     * Retorna os palpites públicos de todos os participantes para uma partida,
     * mas somente após o bloqueio (T-1h). Antes disso retorna lista vazia.
     *
     * @param matchId ID da partida
     * @return Lista de palpites visíveis, ou vazia se ainda aberta
     */
    @Transactional(readOnly = true)
    public List<Prediction> getPublicPredictionsForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + matchId));

        if (!match.isLocked()) {
            return List.of(); // HU-20: palpites só são públicos após bloqueio
        }

        return predictionRepository.findByMatchId(matchId);
    }

    /**
     * Retorna os palpites públicos de uma partida enriquecidos com o displayName do participante.
     * Somente disponível após o bloqueio da partida (T-1h).
     *
     * @param matchId ID da partida
     * @return Lista de {@link PredictionView}, ou vazia se a partida ainda não foi bloqueada
     */
    @Transactional(readOnly = true)
    public List<PredictionView> getPublicPredictionViewsForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + matchId));

        if (!match.isLocked()) {
            return List.of(); // HU-20: palpites só são públicos após bloqueio
        }

        List<Prediction> predictions = predictionRepository.findByMatchId(matchId);
        if (predictions.isEmpty()) {
            return List.of();
        }

        // Carrega todos os usuários necessários em uma única query
        List<Long> userIds = predictions.stream().map(Prediction::getUserId).toList();
        Map<Long, String> displayNameById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));

        return predictions.stream()
                .map(p -> new PredictionView(
                        p.getUserId(),
                        displayNameById.getOrDefault(p.getUserId(), "Participante #" + p.getUserId()),
                        p.getHomeScore(),
                        p.getAwayScore(),
                        p.getPointsAwarded(),
                        p.getScoreReason()
                ))
                .toList();
    }

    /**
     * Retorna todos os palpites de um participante em uma rodada específica.
     */
    @Transactional(readOnly = true)
    public List<Prediction> getUserPredictionsForRound(Long userId, int roundNumber) {
        List<Match> roundMatches = matchRepository.findByRoundNumberOrderByMatchNumberAsc(roundNumber);
        List<Long> matchIds = roundMatches.stream().map(Match::getId).toList();
        return predictionRepository.findByUserId(userId).stream()
                .filter(p -> matchIds.contains(p.getMatchId()))
                .toList();
    }

    /**
     * Verifica se o participante já registrou palpite para a partida informada.
     */
    @Transactional(readOnly = true)
    public boolean hasUserPredicted(Long userId, Long matchId) {
        return predictionRepository.findByUserIdAndMatchId(userId, matchId).isPresent();
    }

    /**
     * Retorna todos os palpites de um participante.
     */
    @Transactional(readOnly = true)
    public List<Prediction> getUserPredictions(Long userId) {
        return predictionRepository.findByUserId(userId);
    }
}
