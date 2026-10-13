package com.bolao2027.modules.scoring.application;

import com.bolao2027.modules.gamification.application.BadgeService;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import com.bolao2027.modules.scoring.domain.ScoreCalculationResult;
import com.bolao2027.modules.scoring.domain.ScoringEngine;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço de aplicação responsável pelo processamento e persistência idempotente
 * das pontuações dos palpites quando os resultados oficiais das partidas são lançados ou corrigidos.
 */
@Service
@Transactional
public class ScoringService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;
    private final ScoringEngine scoringEngine;

    /** Opcional para evitar dependência circular; injetado via setter após construção. */
    @Autowired(required = false)
    private BadgeService badgeService;

    public ScoringService(MatchRepository matchRepository,
                          PredictionRepository predictionRepository,
                          ScoringEngine scoringEngine) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
        this.scoringEngine = scoringEngine;
    }

    /**
     * Calcula e persiste a pontuação de todos os palpites para a partida informada, de forma idempotente.
     */
    public List<Prediction> calculateScoresForMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada: " + matchId));
        return calculateScoresForMatch(match);
    }

    /**
     * Calcula e persiste a pontuação dos palpites para a partida informada.
     */
    public List<Prediction> calculateScoresForMatch(Match match) {
        if (match == null || match.getId() == null) {
            throw new IllegalArgumentException("Partida inválida.");
        }
        if (match.getHomeScore120() == null || match.getAwayScore120() == null) {
            throw new IllegalStateException("A partida não possui placar oficial apurado para cálculo de pontos.");
        }

        List<Prediction> predictions = predictionRepository.findByMatchId(match.getId());
        for (Prediction prediction : predictions) {
            ScoreCalculationResult result = scoringEngine.calculate(prediction, match);
            prediction.applyScore(result.points(), result.reason());
        }

        List<Prediction> saved = predictionRepository.saveAll(predictions);

        // Avalia e concede badges para cada participante após o cálculo de pontos
        for (Prediction prediction : saved) {
            if (badgeService != null) {
                badgeService.evaluateAndAward(prediction.getUserId());
            }
        }

        return saved;
    }

    /**
     * Recalcula idempotentemente as pontuações de todos os palpites de todas as partidas já finalizadas.
     */
    public void recalculateAllScores() {
        List<Match> finishedMatches = matchRepository.findAll().stream()
                .filter(m -> m.getHomeScore120() != null && m.getAwayScore120() != null)
                .toList();

        for (Match match : finishedMatches) {
            calculateScoresForMatch(match);
        }
    }
}
