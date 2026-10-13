package com.bolao2027.modules.ranking.application;

import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import com.bolao2027.modules.ranking.domain.RankingEntry;
import com.bolao2027.modules.ranking.domain.RankingTieBreakerComparator;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.MatchStatus;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela geração do leaderboard (ranking geral e por rodada)
 * com aplicação automática dos 4 critérios de desempate definidos no Ticket 004 (HU-10).
 */
@Service
@Transactional(readOnly = true)
public class RankingService {

    private final PredictionRepository predictionRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final RankingTieBreakerComparator comparator = new RankingTieBreakerComparator();

    public RankingService(PredictionRepository predictionRepository,
                          UserRepository userRepository,
                          MatchRepository matchRepository) {
        this.predictionRepository = predictionRepository;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
    }

    /**
     * Gera o leaderboard geral com todos os participantes ordenados pelos critérios de desempate.
     * Inclui apenas usuários com pelo menos um palpite pontuado.
     *
     * @return Lista de RankingEntry com posições atribuídas
     */
    public List<RankingEntry> buildLeaderboard() {
        List<Match> allMatches = matchRepository.findAll();
        int currentRound = determineCurrentRound(allMatches);

        Map<Long, Match> matchById = allMatches.stream()
                .collect(Collectors.toMap(Match::getId, m -> m));

        List<User> users = userRepository.findAll();
        List<RankingEntry> entries = new ArrayList<>();

        for (User user : users) {
            List<Prediction> predictions = predictionRepository.findByUserId(user.getId());
            List<Prediction> scored = predictions.stream()
                    .filter(p -> p.getPointsAwarded() != null)
                    .toList();

            if (scored.isEmpty()) continue;

            int totalPoints = scored.stream().mapToInt(Prediction::getPointsAwarded).sum();
            int exactCount = (int) scored.stream().filter(p -> p.getPointsAwarded() == 6).count();
            int partialCount = (int) scored.stream().filter(p -> p.getPointsAwarded() == 3).count();

            int prevRoundsPoints = 0;
            if (currentRound > 1) {
                prevRoundsPoints = scored.stream()
                        .filter(p -> {
                            Match m = matchById.get(p.getMatchId());
                            return m != null && m.getRoundNumber() < currentRound;
                        })
                        .mapToInt(Prediction::getPointsAwarded)
                        .sum();
            }

            entries.add(new RankingEntry(
                    user.getId(),
                    user.getDisplayName(),
                    totalPoints,
                    exactCount,
                    partialCount,
                    prevRoundsPoints,
                    0 // posição será atribuída abaixo
            ));
        }

        return assignPositions(entries);
    }

    /**
     * Gera o leaderboard considerando apenas partidas até a rodada especificada (inclusive).
     * Útil para calcular previousRoundsPoints e histórico rodada a rodada.
     *
     * @param maxRound Rodada máxima a considerar (1-7)
     * @return Lista de RankingEntry com posições atribuídas
     */
    public List<RankingEntry> buildLeaderboardUpToRound(int maxRound) {
        List<Match> allMatches = matchRepository.findAll();
        Set<Long> eligibleMatchIds = allMatches.stream()
                .filter(m -> m.getRoundNumber() <= maxRound)
                .map(Match::getId)
                .collect(Collectors.toSet());

        List<User> users = userRepository.findAll();
        List<RankingEntry> entries = new ArrayList<>();

        for (User user : users) {
            List<Prediction> predictions = predictionRepository.findByUserId(user.getId()).stream()
                    .filter(p -> p.getPointsAwarded() != null && eligibleMatchIds.contains(p.getMatchId()))
                    .toList();

            if (predictions.isEmpty()) continue;

            int totalPoints = predictions.stream().mapToInt(Prediction::getPointsAwarded).sum();
            int exactCount = (int) predictions.stream().filter(p -> p.getPointsAwarded() == 6).count();
            int partialCount = (int) predictions.stream().filter(p -> p.getPointsAwarded() == 3).count();

            entries.add(new RankingEntry(
                    user.getId(),
                    user.getDisplayName(),
                    totalPoints,
                    exactCount,
                    partialCount,
                    0,
                    0
            ));
        }

        return assignPositions(entries);
    }

    /**
     * Retorna a entrada de ranking de um participante específico no leaderboard geral.
     *
     * @param userId ID do participante
     * @return Optional com o RankingEntry do participante, ou empty se não ranqueado
     */
    public Optional<RankingEntry> getUserRanking(Long userId) {
        return buildLeaderboard().stream()
                .filter(e -> e.userId().equals(userId))
                .findFirst();
    }

    // ─── Privado ─────────────────────────────────────────────────────────────

    /**
     * Determina a rodada atual com base nas partidas finalizadas.
     * Retorna 1 se nenhuma partida foi encerrada ainda.
     */
    private int determineCurrentRound(List<Match> matches) {
        return matches.stream()
                .filter(m -> m.getStatus() == MatchStatus.FINISHED)
                .mapToInt(Match::getRoundNumber)
                .max()
                .orElse(1);
    }

    /**
     * Ordena a lista pelo comparator e atribui posições,
     * tratando empates de pontuação total com a mesma posição.
     */
    private List<RankingEntry> assignPositions(List<RankingEntry> entries) {
        List<RankingEntry> sorted = entries.stream()
                .sorted(comparator)
                .collect(Collectors.toCollection(ArrayList::new));

        List<RankingEntry> result = new ArrayList<>();
        int pos = 1;
        for (int i = 0; i < sorted.size(); i++) {
            RankingEntry current = sorted.get(i);
            if (i > 0 && current.isTiedWith(sorted.get(i - 1))) {
                // empate em pontuação total: mantém a posição do anterior
                result.add(current.withPosition(result.get(i - 1).position()));
            } else {
                result.add(current.withPosition(pos));
            }
            pos++;
        }
        return result;
    }
}
