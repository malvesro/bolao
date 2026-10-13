package com.bolao2027.modules.scoring.domain;

import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.tournament.domain.Match;
import org.springframework.stereotype.Component;

/**
 * Motor de cálculo de pontuação para o Bolão Copa 2027.
 *
 * Tabela oficial de pontuação (decrescente e excludente):
 * 6 pts: Acerto do placar exato
 * 3 pts: Acerto do vencedor/empate + gols de 1 equipe
 * 2 pts: Acerto apenas do resultado (vitória, derrota ou empate)
 * 1 pt:  Acerto apenas dos gols de 1 equipe (errou o resultado geral)
 * 0 pts: Demais combinações (nenhum acerto)
 *
 * REGRA OBRIGATÓRIA DE MATA-MATA (HU-07):
 * O resultado considerado oficial para o bolão é o placar ao final dos 120 minutos de bola
 * rolando (90 min regulamentares + prorrogação). Em caso de decisão por pênaltis, o resultado
 * oficial no bolão é EMPATE (os gols de pênaltis não são somados ao placar da partida).
 */
@Component
public class ScoringEngine {

    /**
     * Calcula a pontuação contra o placar apurado aos 120 minutos (homeScore120, awayScore120).
     *
     * @param predHome Placar palpitado do mandante
     * @param predAway Placar palpitado do visitante
     * @param actualHome120 Placar oficial do mandante aos 120 minutos
     * @param actualAway120 Placar oficial do visitante aos 120 minutos
     * @return ScoreCalculationResult com pontos e motivo legível
     */
    public ScoreCalculationResult calculate(Integer predHome, Integer predAway,
                                            Integer actualHome120, Integer actualAway120) {
        if (predHome == null || predAway == null || actualHome120 == null || actualAway120 == null) {
            throw new IllegalArgumentException("Placares não podem ser nulos para cálculo de pontuação.");
        }
        if (predHome < 0 || predAway < 0 || actualHome120 < 0 || actualAway120 < 0) {
            throw new IllegalArgumentException("Placares não podem ser negativos.");
        }

        // 1. Placar exato (6 pts)
        if (predHome.equals(actualHome120) && predAway.equals(actualAway120)) {
            return ScoreCalculationResult.exactScore();
        }

        int actualDiff = Integer.compare(actualHome120, actualAway120);
        int predDiff = Integer.compare(predHome, predAway);
        boolean sameOutcome = (actualDiff == predDiff);

        boolean hitOneTeamGoals = predHome.equals(actualHome120) || predAway.equals(actualAway120);

        // 2. Acerto do vencedor/empate + gols de 1 equipe (3 pts)
        if (sameOutcome && hitOneTeamGoals) {
            return ScoreCalculationResult.winnerAndGoals();
        }

        // 3. Acerto apenas do resultado (2 pts)
        if (sameOutcome) {
            return ScoreCalculationResult.resultOnly();
        }

        // 4. Acerto apenas dos gols de 1 equipe (1 pt)
        if (hitOneTeamGoals) {
            return ScoreCalculationResult.oneTeamGoals();
        }

        // 5. Demais combinações (0 pts)
        return ScoreCalculationResult.noHit();
    }

    /**
     * Sobrecarga para calcular a pontuação de um palpite diretamente a partir da entidade Match.
     * Utiliza estritamente homeScore120 e awayScore120 do Match, garantindo a conformidade com a
     * regra de mata-mata (HU-07), ignorando qualquer contagem de pênaltis.
     */
    public ScoreCalculationResult calculate(Prediction prediction, Match match) {
        if (prediction == null || match == null) {
            throw new IllegalArgumentException("Palpite e partida não podem ser nulos.");
        }
        if (prediction.getHomeScore() == null || prediction.getAwayScore() == null) {
            throw new IllegalArgumentException("Palpite contém placares não definidos.");
        }
        if (match.getHomeScore120() == null || match.getAwayScore120() == null) {
            throw new IllegalStateException("A partida #" + match.getMatchNumber() + " ainda não possui placar oficial apurado.");
        }

        return calculate(
                prediction.getHomeScore(),
                prediction.getAwayScore(),
                match.getHomeScore120(),
                match.getAwayScore120()
        );
    }
}
