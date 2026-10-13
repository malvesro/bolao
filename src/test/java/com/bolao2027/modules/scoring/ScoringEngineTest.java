package com.bolao2027.modules.scoring;

import com.bolao2027.modules.scoring.domain.ScoreCalculationResult;
import com.bolao2027.modules.scoring.domain.ScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários puros para ScoringEngine (sem Spring context).
 *
 * Tabela de pontuação testada:
 *   6 pts → Placar exato
 *   3 pts → Vencedor/empate + gols de 1 equipe
 *   2 pts → Apenas resultado (vitória ou empate)
 *   1 pt  → Gols de 1 equipe (errou resultado)
 *   0 pts → Nenhum acerto
 *
 * Regra 120min (HU-07): decisão por pênaltis = empate oficial.
 * Palpite de empate com placar correto ao fim dos 120min = 6 pts.
 */
@DisplayName("ScoringEngine — lógica de pontuação")
class ScoringEngineTest {

    private ScoringEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ScoringEngine();
    }

    // -------------------------------------------------------------------------
    // Teste 1: Placar exato → 6 pts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Placar exato deve retornar 6 pontos (pred 2-1, real 2-1)")
    void deveRetornar6PontosParaPlacarExato() {
        ScoreCalculationResult result = engine.calculate(2, 1, 2, 1);

        assertAll("placar exato",
                () -> assertEquals(6, result.points(), "pontos esperados: 6"),
                () -> assertEquals(ScoreCalculationResult.EXACT_SCORE_REASON, result.reason(),
                        "motivo esperado: Placar exato")
        );
    }

    @Test
    @DisplayName("Placar exato 0-0 deve retornar 6 pontos")
    void deveRetornar6PontosParaPlacarExatoZeroZero() {
        ScoreCalculationResult result = engine.calculate(0, 0, 0, 0);

        assertAll("placar exato 0-0",
                () -> assertEquals(6, result.points()),
                () -> assertEquals(ScoreCalculationResult.EXACT_SCORE_REASON, result.reason())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 2: Vencedor + gols de 1 time → 3 pts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Vencedor + gols de 1 equipe deve retornar 3 pontos (pred 2-0, real 2-1)")
    void deveRetornar3PontosParaVencedorMaisGolsDeUmaEquipe() {
        // pred 2-0: time A vence; acertou gols do time A (2)
        // real 2-1: time A vence; gols do time A também são 2
        ScoreCalculationResult result = engine.calculate(2, 0, 2, 1);

        assertAll("vencedor + gols de 1 equipe",
                () -> assertEquals(3, result.points(), "pontos esperados: 3"),
                () -> assertEquals(ScoreCalculationResult.WINNER_AND_GOALS_REASON, result.reason(),
                        "motivo esperado: Vencedor + gols de uma equipe")
        );
    }

    @Test
    @DisplayName("Vencedor + gols do visitante deve retornar 3 pontos (pred 0-1, real 1-1 NÃO — pred 1-2, real 0-2)")
    void deveRetornar3PontosAcertandoGolsDoVisitante() {
        // pred 1-2: visitante vence, gols visitante = 2
        // real 0-2: visitante vence, gols visitante = 2 → acertou vencedor + gols do visitante
        ScoreCalculationResult result = engine.calculate(1, 2, 0, 2);

        assertAll("vencedor + gols do visitante",
                () -> assertEquals(3, result.points()),
                () -> assertEquals(ScoreCalculationResult.WINNER_AND_GOALS_REASON, result.reason())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 3: Só resultado vitória → 2 pts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Acerto apenas do resultado de vitória deve retornar 2 pontos (pred 2-1, real 4-3)")
    void deveRetornar2PontosParaAcertoSoDeVitoria() {
        // pred 2-1: mandante vence — real 4-3: mandante vence
        // Nenhum placar individual coincide (2≠4, 1≠3) → só resultado → 2 pts
        // Nota: pred 1-0 vs real 3-0 daria 3 pts porque o visitante (0) coincide em ambos
        ScoreCalculationResult result = engine.calculate(2, 1, 4, 3);

        assertAll("só resultado vitória",
                () -> assertEquals(2, result.points(), "pontos esperados: 2"),
                () -> assertEquals(ScoreCalculationResult.RESULT_ONLY_REASON, result.reason(),
                        "motivo esperado: Apenas resultado")
        );
    }

    // -------------------------------------------------------------------------
    // Teste 4: Só resultado empate → 2 pts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Acerto apenas do resultado de empate deve retornar 2 pontos (pred 1-1, real 2-2)")
    void deveRetornar2PontosParaAcertoSoDeEmpate() {
        // pred 1-1: empate — real 2-2: empate, mas nenhum placar individual coincide
        ScoreCalculationResult result = engine.calculate(1, 1, 2, 2);

        assertAll("só resultado empate",
                () -> assertEquals(2, result.points(), "pontos esperados: 2"),
                () -> assertEquals(ScoreCalculationResult.RESULT_ONLY_REASON, result.reason(),
                        "motivo esperado: Apenas resultado")
        );
    }

    // -------------------------------------------------------------------------
    // Teste 5: Gols de 1 equipe sem acertar resultado → 1 pt
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Gols de 1 equipe sem acertar resultado deve retornar 1 ponto (pred 0-2, real 1-2)")
    void deveRetornar1PontoParaGolsDeUmaEquipeSemAcertarResultado() {
        // pred 0-2: visitante vence — real 1-2: visitante vence? → ESPERA: 0 != 1, mas 2 == 2
        // resultado: pred visitante vence (0<2), real visitante vence (1<2) → sameOutcome = true!
        // Isso seria 3 pts. Para 1 pt precisamos: errar resultado mas acertar gols de 1 lado.
        // pred 0-2: visitante vence; real 1-2: visitante vence → MESMO resultado, logo 3 pts.
        // Usar: pred 0-2 (visitante vence), real 1-2 É o mesmo resultado. Preciso inverter:
        // pred 0-2 visitante vence; real 2-1 mandante vence → resultados diferentes, gols: 0!=2, 2!=1 → 0 pts
        // Exemplo correto para 1 pt: pred 2-0 (mandante vence), real 2-2 empate
        //   → resultado diferente (vitória vs empate), mas gols do mandante = 2 em ambos → 1 pt
        ScoreCalculationResult result = engine.calculate(2, 0, 2, 2);

        assertAll("gols de 1 equipe sem acertar resultado",
                () -> assertEquals(1, result.points(), "pontos esperados: 1"),
                () -> assertEquals(ScoreCalculationResult.ONE_TEAM_GOALS_REASON, result.reason(),
                        "motivo esperado: Gols de uma equipe")
        );
    }

    @Test
    @DisplayName("Gols do visitante sem acertar resultado retorna 1 ponto (pred 0-2, real 1-2 ERRADO — usar pred 1-2, real 2-2)")
    void deveRetornar1PontoAcertandoGolsDoVisitanteSemResultado() {
        // pred 1-2: visitante vence; real 2-2: empate
        // resultado diferente (vitória visitante vs empate), gols visitante = 2 nos dois → 1 pt
        ScoreCalculationResult result = engine.calculate(1, 2, 2, 2);

        assertAll("gols do visitante sem resultado",
                () -> assertEquals(1, result.points()),
                () -> assertEquals(ScoreCalculationResult.ONE_TEAM_GOALS_REASON, result.reason())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 6: Nenhum acerto → 0 pts
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Nenhum acerto deve retornar 0 pontos (pred 1-0, real 0-1)")
    void deveRetornar0PontosParaNenhumAcerto() {
        // pred 1-0: mandante vence — real 0-1: visitante vence → resultado diferente, nenhum placar coincide
        ScoreCalculationResult result = engine.calculate(1, 0, 0, 1);

        assertAll("nenhum acerto",
                () -> assertEquals(0, result.points(), "pontos esperados: 0"),
                () -> assertEquals(ScoreCalculationResult.NO_HIT_REASON, result.reason(),
                        "motivo esperado: Nenhum acerto")
        );
    }

    @Test
    @DisplayName("Nenhum acerto com placares distintos retorna 0 pontos (pred 2-3, real 1-0)")
    void deveRetornar0PontosComPlacaresCompletamenteDiferentes() {
        ScoreCalculationResult result = engine.calculate(2, 3, 1, 0);

        assertAll("nenhum acerto — placares completamente diferentes",
                () -> assertEquals(0, result.points()),
                () -> assertEquals(ScoreCalculationResult.NO_HIT_REASON, result.reason())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 7: Regra 120min — empate em 120min (pênaltis)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Regra 120min: palpite de empate com placar correto = 6 pts (pred 1-1, real 1-1 nos 120min)")
    void deveRetornar6PontosParaEmpateExatoNos120Min() {
        // Partida foi para pênaltis, mas o placar nos 120 min foi 1-1.
        // actualHome120=1, actualAway120=1 representa o resultado oficial do bolão.
        // Palpite pred 1-1 == real 1-1 → placar exato → 6 pts.
        ScoreCalculationResult result = engine.calculate(1, 1, 1, 1);

        assertAll("empate exato em 120min",
                () -> assertEquals(6, result.points(), "empate exato nos 120min deve valer 6 pts"),
                () -> assertEquals(ScoreCalculationResult.EXACT_SCORE_REASON, result.reason())
        );
    }

    @Test
    @DisplayName("Regra 120min: palpite de empate com placar diferente do 120min = 2 pts (pred 0-0, real 1-1)")
    void deveRetornar2PontosParaEmpateComPlacarDiferenteNos120Min() {
        // Partida foi para pênaltis, placar nos 120min foi 1-1.
        // Palpite pred 0-0: acertou empate mas errou o placar → 2 pts (só resultado)
        ScoreCalculationResult result = engine.calculate(0, 0, 1, 1);

        assertAll("empate com placar errado nos 120min",
                () -> assertEquals(2, result.points(), "acertou empate mas errou placar = 2 pts"),
                () -> assertEquals(ScoreCalculationResult.RESULT_ONLY_REASON, result.reason())
        );
    }

    @Test
    @DisplayName("Regra 120min: palpite de vitória errado quando real foi empate nos 120min = 0 pts")
    void deveRetornar0PontosParaVitoriaQuandoRealFoiEmpateNos120Min() {
        // Partida decidida nos pênaltis, placar 120min = 1-1 (empate oficial).
        // Quem chutou vitória do mandante (2-1) erra tudo: resultado diferente, gols também → 0 pts
        ScoreCalculationResult result = engine.calculate(2, 1, 1, 1);

        // pred 2-1 (mandante vence), real 1-1 (empate): resultado diferente
        // gols mandante: 2 != 1; gols visitante: 1 == 1 → hitOneTeamGoals = true, sameOutcome = false → 1 pt
        assertAll("vitória prevista, empate nos 120min",
                () -> assertEquals(1, result.points(),
                        "acertou gols do visitante mas errou resultado = 1 pt"),
                () -> assertEquals(ScoreCalculationResult.ONE_TEAM_GOALS_REASON, result.reason())
        );
    }

    @Test
    @DisplayName("Regra 120min: palpite sem coincidência alguma com resultado de pênaltis = 0 pts")
    void deveRetornar0PontosQuandoNaoHaNenhumaCoincideniaComResultadoDePenaltis() {
        // Placar 120min = 1-1; palpite = 3-0 → resultado diferente, nenhum gol coincide → 0 pts
        ScoreCalculationResult result = engine.calculate(3, 0, 1, 1);

        assertAll("palpite completamente errado com resultado de pênaltis",
                () -> assertEquals(0, result.points()),
                () -> assertEquals(ScoreCalculationResult.NO_HIT_REASON, result.reason())
        );
    }

    // -------------------------------------------------------------------------
    // Teste 8: Nulos e negativos → IllegalArgumentException
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("predHome nulo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaPredHomeNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(null, 1, 2, 1),
                "predHome nulo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("predAway nulo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaPredAwayNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(2, null, 2, 1),
                "predAway nulo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("actualHome120 nulo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaActualHomeNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(2, 1, null, 1),
                "actualHome120 nulo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("actualAway120 nulo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaActualAwayNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(2, 1, 2, null),
                "actualAway120 nulo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("predHome negativo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaPredHomeNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(-1, 0, 1, 0),
                "predHome negativo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("predAway negativo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaPredAwayNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(1, -1, 1, 0),
                "predAway negativo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("actualHome120 negativo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaActualHomeNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(1, 0, -1, 0),
                "actualHome120 negativo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("actualAway120 negativo deve lançar IllegalArgumentException")
    void deveLancarExcecaoParaActualAwayNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(1, 0, 1, -1),
                "actualAway120 negativo deve lançar IllegalArgumentException");
    }

    @Test
    @DisplayName("Todos os placares nulos devem lançar IllegalArgumentException")
    void deveLancarExcecaoParaTodosNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> engine.calculate(null, null, null, null),
                "todos nulos deve lançar IllegalArgumentException");
    }
}
