package com.bolao2027.modules.ranking.domain;

/**
 * Registro imutável representando a entrada de um participante no ranking do bolão.
 *
 * @param userId               ID do participante
 * @param displayName          Nome de exibição do participante
 * @param totalPoints          Total de pontos acumulados
 * @param exactScoreCount      Quantidade de acertos de placar exato (6 pts)
 * @param partialScoreCount    Quantidade de acertos de vencedor + gols de 1 time (3 pts)
 * @param previousRoundsPoints Pontos acumulados até a rodada anterior (para desempate)
 * @param position             Posição no ranking (1-indexed; empates compartilham posição)
 */
public record RankingEntry(
        Long userId,
        String displayName,
        int totalPoints,
        int exactScoreCount,
        int partialScoreCount,
        int previousRoundsPoints,
        int position
) {

    /**
     * Verifica se dois participantes estão empatados em pontuação total.
     * A desambiguação dentro do empate é feita pelo {@link RankingTieBreakerComparator}.
     */
    public boolean isTiedWith(RankingEntry other) {
        return other != null && this.totalPoints == other.totalPoints;
    }

    /**
     * Retorna uma nova entrada com a posição atualizada.
     */
    public RankingEntry withPosition(int newPosition) {
        return new RankingEntry(userId, displayName, totalPoints, exactScoreCount,
                partialScoreCount, previousRoundsPoints, newPosition);
    }
}
