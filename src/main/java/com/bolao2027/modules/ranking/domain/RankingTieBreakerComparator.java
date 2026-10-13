package com.bolao2027.modules.ranking.domain;

import java.text.Normalizer;
import java.util.Comparator;

/**
 * Comparador para o ranking do bolão, aplicando os 4 critérios de desempate
 * na ordem oficial definida nos requisitos (HU-10, Ticket 004):
 *
 * 1º Maior pontuação total DESC
 * 2º Maior quantidade de placares exatos (6 pts) DESC
 * 3º Maior quantidade de acertos parciais (3 pts) DESC
 * 4º Maior pontuação em rodadas anteriores DESC
 * 5º Ordem alfabética pelo nome normalizado (sem acentos, minúsculo) ASC
 */
public class RankingTieBreakerComparator implements Comparator<RankingEntry> {

    @Override
    public int compare(RankingEntry a, RankingEntry b) {
        // 1º critério: pontuação total DESC
        int cmp = Integer.compare(b.totalPoints(), a.totalPoints());
        if (cmp != 0) return cmp;

        // 2º critério: placares exatos (6 pts) DESC
        cmp = Integer.compare(b.exactScoreCount(), a.exactScoreCount());
        if (cmp != 0) return cmp;

        // 3º critério: acertos parciais (3 pts) DESC
        cmp = Integer.compare(b.partialScoreCount(), a.partialScoreCount());
        if (cmp != 0) return cmp;

        // 4º critério: pontos em rodadas anteriores DESC
        cmp = Integer.compare(b.previousRoundsPoints(), a.previousRoundsPoints());
        if (cmp != 0) return cmp;

        // 5º critério: nome normalizado ASC (sem acentos, case-insensitive)
        return normalize(a.displayName()).compareTo(normalize(b.displayName()));
    }

    /**
     * Normaliza o nome removendo acentos e convertendo para minúsculas,
     * conforme requisito de desempate por ordem alfabética sem distinção de maiúsculas/minúsculas.
     */
    static String normalize(String name) {
        if (name == null) return "";
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
    }
}
