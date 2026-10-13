package com.bolao2027.modules.prediction.application;

/**
 * DTO para exibição pública de palpites com o nome do participante.
 * Usado em vez da entidade Prediction para evitar expor o userId numérico.
 */
public record PredictionView(
        Long userId,
        String displayName,
        Integer homeScore,
        Integer awayScore,
        Integer pointsAwarded,
        String scoreReason
) {}
