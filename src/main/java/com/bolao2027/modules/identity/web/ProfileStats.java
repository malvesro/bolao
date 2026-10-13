package com.bolao2027.modules.identity.web;

/**
 * Estatísticas de desempenho do participante no bolão.
 *
 * @param totalPredicted  Total de palpites enviados
 * @param scoredCount     Total de palpites com pontuação atribuída (incluindo 0)
 * @param exactCount      Palpites com placar exato (6 pts)
 * @param partialCount    Palpites com resultado + pelo menos um time certo (3 pts)
 * @param resultCount     Palpites com resultado correto apenas (2 pts)
 * @param oneTeamCount    Palpites com gols de 1 time corretos (1 pt)
 * @param zeroCount       Palpites sem nenhum ponto (0 pts)
 */
public record ProfileStats(
    int totalPredicted,
    int scoredCount,
    int exactCount,
    int partialCount,
    int resultCount,
    int oneTeamCount,
    int zeroCount
) {}
