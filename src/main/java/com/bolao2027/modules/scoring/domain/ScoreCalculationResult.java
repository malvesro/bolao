package com.bolao2027.modules.scoring.domain;

/**
 * Resultado do cálculo de pontuação de um palpite segundo o sistema oficial do bolão:
 * 6 pts: Placar exato
 * 3 pts: Vencedor + gols de uma equipe
 * 2 pts: Apenas resultado
 * 1 pt:  Gols de uma equipe
 * 0 pts: Nenhum acerto
 */
public record ScoreCalculationResult(int points, String reason) {

    public static final String EXACT_SCORE_REASON = "Placar exato";
    public static final String WINNER_AND_GOALS_REASON = "Vencedor + gols de uma equipe";
    public static final String RESULT_ONLY_REASON = "Apenas resultado";
    public static final String ONE_TEAM_GOALS_REASON = "Gols de uma equipe";
    public static final String NO_HIT_REASON = "Nenhum acerto";

    public static ScoreCalculationResult exactScore() {
        return new ScoreCalculationResult(6, EXACT_SCORE_REASON);
    }

    public static ScoreCalculationResult winnerAndGoals() {
        return new ScoreCalculationResult(3, WINNER_AND_GOALS_REASON);
    }

    public static ScoreCalculationResult resultOnly() {
        return new ScoreCalculationResult(2, RESULT_ONLY_REASON);
    }

    public static ScoreCalculationResult oneTeamGoals() {
        return new ScoreCalculationResult(1, ONE_TEAM_GOALS_REASON);
    }

    public static ScoreCalculationResult noHit() {
        return new ScoreCalculationResult(0, NO_HIT_REASON);
    }

    public int getPoints() {
        return points;
    }

    public String getReason() {
        return reason;
    }
}
