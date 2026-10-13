package com.bolao2027.modules.ranking;

import com.bolao2027.modules.ranking.domain.RankingEntry;
import com.bolao2027.modules.ranking.domain.RankingTieBreakerComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários puros para RankingTieBreakerComparator (sem Spring context).
 *
 * Critérios de desempate (ordem oficial HU-10, Ticket 004):
 *   1º Maior pontuação total DESC
 *   2º Maior quantidade de placares exatos (6 pts) DESC
 *   3º Maior quantidade de acertos parciais (3 pts) DESC
 *   4º Maior pontuação em rodadas anteriores DESC
 *   5º Ordem alfabética pelo nome normalizado (sem acentos, minúsculo) ASC
 *
 * RankingEntry record: (userId, displayName, totalPoints, exactScoreCount,
 *                       partialScoreCount, previousRoundsPoints, position)
 */
@DisplayName("RankingTieBreakerComparator — critérios de desempate")
class RankingTieBreakerTest {

    private RankingTieBreakerComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new RankingTieBreakerComparator();
    }

    // Fábrica auxiliar para criar RankingEntry sem poluir os testes
    private static RankingEntry entry(Long userId, String name, int totalPoints,
                                      int exactCount, int partialCount,
                                      int previousPoints, int position) {
        return new RankingEntry(userId, name, totalPoints, exactCount, partialCount,
                previousPoints, position);
    }

    // -------------------------------------------------------------------------
    // Teste 1: Maior pontuação total vence
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("1º critério: maior pontuação total deve vir primeiro")
    void maiorPontuacaoTotalDeveVencerDesempate() {
        RankingEntry maior = entry(1L, "Carlos", 30, 2, 3, 20, 1);
        RankingEntry menor = entry(2L, "Daniel", 25, 5, 5, 25, 2);

        int resultado = comparator.compare(maior, menor);

        assertAll("maior pontuação total vence",
                () -> assertTrue(resultado < 0,
                        "quem tem mais pontos totais deve ser 'menor' no comparador (vem antes = posição melhor)"),
                () -> assertTrue(comparator.compare(menor, maior) > 0,
                        "sentido inverso deve ser positivo")
        );
    }

    @Test
    @DisplayName("1º critério: ordenação de lista por pontuação total")
    void deveOrdenarListaPorPontuacaoTotal() {
        RankingEntry e1 = entry(1L, "Ana",    10, 0, 1, 5,  3);
        RankingEntry e2 = entry(2L, "Bruno",  20, 1, 2, 10, 2);
        RankingEntry e3 = entry(3L, "Carlos", 30, 2, 3, 15, 1);

        List<RankingEntry> lista = Arrays.asList(e1, e3, e2);
        lista.sort(comparator);

        assertAll("ordenação por pontos",
                () -> assertEquals(30, lista.get(0).totalPoints(), "1º lugar: 30 pts"),
                () -> assertEquals(20, lista.get(1).totalPoints(), "2º lugar: 20 pts"),
                () -> assertEquals(10, lista.get(2).totalPoints(), "3º lugar: 10 pts")
        );
    }

    // -------------------------------------------------------------------------
    // Teste 2: Empate em pontos — quem tem mais exatos (6 pts) vence
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("2º critério: empate em pontos, quem tem mais placares exatos vence")
    void empatePontosMaisExatosVence() {
        RankingEntry maisExatos  = entry(1L, "Eva",   20, 3, 1, 10, 1);
        RankingEntry menosExatos = entry(2L, "Fábio", 20, 1, 4, 10, 1);

        int resultado = comparator.compare(maisExatos, menosExatos);

        assertAll("mais exatos vence em empate de pontos",
                () -> assertTrue(resultado < 0,
                        "quem tem mais exatos deve vir antes quando pontos são iguais"),
                () -> assertEquals(20, maisExatos.totalPoints()),
                () -> assertEquals(20, menosExatos.totalPoints()),
                () -> assertEquals(3, maisExatos.exactScoreCount()),
                () -> assertEquals(1, menosExatos.exactScoreCount())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 3: Empate em pontos e exatos — quem tem mais parciais (3 pts) vence
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("3º critério: empate em pontos e exatos, quem tem mais parciais vence")
    void empatePontosEExatosMaisParciaisVence() {
        RankingEntry maisParciais  = entry(1L, "Gabriela", 20, 2, 4, 10, 1);
        RankingEntry menosParciais = entry(2L, "Henrique", 20, 2, 1, 10, 1);

        int resultado = comparator.compare(maisParciais, menosParciais);

        assertAll("mais parciais vence em empate de pontos e exatos",
                () -> assertTrue(resultado < 0,
                        "quem tem mais parciais deve vir antes quando pontos e exatos são iguais"),
                () -> assertEquals(20, maisParciais.totalPoints()),
                () -> assertEquals(20, menosParciais.totalPoints()),
                () -> assertEquals(2, maisParciais.exactScoreCount()),
                () -> assertEquals(2, menosParciais.exactScoreCount()),
                () -> assertEquals(4, maisParciais.partialScoreCount()),
                () -> assertEquals(1, menosParciais.partialScoreCount())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 4: Empate em tudo exceto previousRoundsPoints — mais pontos anteriores vence
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("4º critério: empate em tudo exceto previousRoundsPoints, mais pontos anteriores vence")
    void empateTudoExcetoRodadasAnterioresMaisPontosAnterioresVence() {
        RankingEntry maisPrevious  = entry(1L, "Isabela", 20, 2, 3, 15, 1);
        RankingEntry menosPrevious = entry(2L, "João",    20, 2, 3,  5, 1);

        int resultado = comparator.compare(maisPrevious, menosPrevious);

        assertAll("mais pontos anteriores vence",
                () -> assertTrue(resultado < 0,
                        "quem tem mais pontos anteriores deve vir antes"),
                () -> assertEquals(20, maisPrevious.totalPoints()),
                () -> assertEquals(20, menosPrevious.totalPoints()),
                () -> assertEquals(2, maisPrevious.exactScoreCount()),
                () -> assertEquals(2, menosPrevious.exactScoreCount()),
                () -> assertEquals(3, maisPrevious.partialScoreCount()),
                () -> assertEquals(3, menosPrevious.partialScoreCount()),
                () -> assertEquals(15, maisPrevious.previousRoundsPoints()),
                () -> assertEquals(5,  menosPrevious.previousRoundsPoints())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 5: Empate completo — ordem alfabética ASC (sem acentos, case-insensitive)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("5º critério: empate completo, ordem alfabética ASC — 'Álvaro' < 'Beatriz'")
    void empateCompletoOrdemAlfabeticaAscAlvaroBeatriz() {
        // "Álvaro" normaliza para "alvaro", "Beatriz" para "beatriz" → "alvaro" < "beatriz"
        RankingEntry alvaro  = entry(1L, "Álvaro",  20, 2, 3, 10, 1);
        RankingEntry beatriz = entry(2L, "Beatriz", 20, 2, 3, 10, 1);

        int resultado = comparator.compare(alvaro, beatriz);

        assertAll("ordem alfabética: Álvaro antes de Beatriz",
                () -> assertTrue(resultado < 0,
                        "'Álvaro' normalizado ('alvaro') deve preceder 'beatriz' na ordem alfabética ASC"),
                () -> assertTrue(comparator.compare(beatriz, alvaro) > 0,
                        "sentido inverso deve ser positivo")
        );
    }

    @Test
    @DisplayName("5º critério: empate completo com mesmo nome normalizado retorna 0")
    void empateCompletoComMesmoNomeRetornaZero() {
        RankingEntry a = entry(1L, "Carlos", 15, 1, 2, 8, 1);
        RankingEntry b = entry(2L, "Carlos", 15, 1, 2, 8, 1);

        assertEquals(0, comparator.compare(a, b),
                "entradas completamente idênticas devem retornar 0");
    }

    @Test
    @DisplayName("5º critério: nomes com case diferente são tratados igualmente — 'CARLOS' == 'carlos'")
    void empateCompletoNomeCaseInsensitive() {
        RankingEntry upper = entry(1L, "CARLOS", 15, 1, 2, 8, 1);
        RankingEntry lower = entry(2L, "carlos", 15, 1, 2, 8, 1);

        assertEquals(0, comparator.compare(upper, lower),
                "nomes idênticos em case diferente devem normalizar para o mesmo valor");
    }

    // -------------------------------------------------------------------------
    // Teste 6: Normalize — comportamento verificado via ordenação do comparador
    //
    // RankingTieBreakerComparator.normalize() é package-private (visível apenas em
    // com.bolao2027.modules.ranking.domain). Os testes abaixo verificam o efeito
    // da normalização através do comparador, que é a interface pública da lógica.
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("normalize via compare: 'Álvaro' e 'alvaro' são tratados como equivalentes (empate)")
    void normalizaAlvaroEquivalenteAAlvaroMinusculo() {
        // Ambas as entradas são idênticas exceto pela grafia do nome.
        // Se a normalização remover o acento e aplicar lowercase corretamente,
        // "Álvaro" e "alvaro" produzem a mesma string normalizada → compare == 0.
        RankingEntry comAcento    = entry(1L, "Álvaro", 10, 1, 1, 5, 1);
        RankingEntry semAcento    = entry(2L, "alvaro", 10, 1, 1, 5, 1);

        assertEquals(0, comparator.compare(comAcento, semAcento),
                "'Álvaro' deve normalizar para 'alvaro', igual a 'alvaro' já em minúsculo");
    }

    @Test
    @DisplayName("normalize via compare: 'ÂNGELA' normaliza igual a 'angela'")
    void normalizaAngelaEquivalenteAAngelaSemAcento() {
        RankingEntry comAcento = entry(1L, "ÂNGELA", 10, 1, 1, 5, 1);
        RankingEntry semAcento = entry(2L, "angela", 10, 1, 1, 5, 1);

        assertEquals(0, comparator.compare(comAcento, semAcento),
                "'ÂNGELA' deve normalizar para 'angela'");
    }

    @Test
    @DisplayName("normalize via compare: 'Renée' normaliza igual a 'renee'")
    void normalizaReneeEquivalenteAReneeSemAcento() {
        RankingEntry comAcento = entry(1L, "Renée", 10, 1, 1, 5, 1);
        RankingEntry semAcento = entry(2L, "renee", 10, 1, 1, 5, 1);

        assertEquals(0, comparator.compare(comAcento, semAcento),
                "'Renée' deve normalizar para 'renee'");
    }

    @Test
    @DisplayName("normalize via compare: nomes com acentos distintos são ordenados corretamente — 'Álvaro' < 'Beatriz'")
    void normalizaOrdemAlfabeticaComAcento() {
        // 'Álvaro' → 'alvaro'; 'Beatriz' → 'beatriz'; 'alvaro' < 'beatriz' na ordem ASCII/Unicode
        RankingEntry alvaro  = entry(1L, "Álvaro",  10, 1, 1, 5, 1);
        RankingEntry beatriz = entry(2L, "Beatriz", 10, 1, 1, 5, 1);

        assertTrue(comparator.compare(alvaro, beatriz) < 0,
                "'Álvaro' normalizado deve preceder 'Beatriz' normalizado");
    }

    @Test
    @DisplayName("normalize via compare: nome null é tratado como string vazia (vem antes de qualquer nome)")
    void normalizaNullTratadoComoStringVazia() {
        // null normaliza para "" que é menor que qualquer nome não-vazio
        RankingEntry comNull   = entry(1L, null,    10, 1, 1, 5, 1);
        RankingEntry comNome   = entry(2L, "Ana",   10, 1, 1, 5, 1);

        assertTrue(comparator.compare(comNull, comNome) < 0,
                "nome null (normalizado para '') deve preceder 'ana' na ordem alfabética");
    }

    // -------------------------------------------------------------------------
    // Teste extra: ordenação completa com todos os critérios em cadeia
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Ordenação completa respeitando todos os 5 critérios em cascata")
    void deveOrdenarRespeitandoTodosOsCriteriosEmCascata() {
        // e1: maior pontuação → 1º
        // e2: mesmos pontos que e3, mais exatos → 2º
        // e3: mesmos pontos que e2, menos exatos, mais parciais que e4 → 3º
        // e4: menos pontos → 4º
        RankingEntry e1 = entry(1L, "Zebra",   30, 1, 1, 10, 0);
        RankingEntry e2 = entry(2L, "Marta",   20, 5, 0, 10, 0);
        RankingEntry e3 = entry(3L, "Larissa", 20, 2, 4, 10, 0);
        RankingEntry e4 = entry(4L, "Pedro",   10, 3, 3, 10, 0);

        List<RankingEntry> lista = Arrays.asList(e4, e2, e1, e3);
        lista.sort(comparator);

        assertAll("ordenação completa 4 entradas",
                () -> assertEquals("Zebra",   lista.get(0).displayName(), "1º: Zebra (30 pts)"),
                () -> assertEquals("Marta",   lista.get(1).displayName(), "2º: Marta (20 pts, 5 exatos)"),
                () -> assertEquals("Larissa", lista.get(2).displayName(), "3º: Larissa (20 pts, 2 exatos, 4 parciais)"),
                () -> assertEquals("Pedro",   lista.get(3).displayName(), "4º: Pedro (10 pts)")
        );
    }
}
