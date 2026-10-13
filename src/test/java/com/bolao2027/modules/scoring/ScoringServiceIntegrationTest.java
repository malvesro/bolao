package com.bolao2027.modules.scoring;

import com.bolao2027.modules.identity.domain.Role;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import com.bolao2027.modules.scoring.application.ScoringService;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.MatchStatus;
import com.bolao2027.modules.tournament.domain.Stage;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para ScoringService usando banco H2 in-memory.
 * Valida o cálculo e persistência das pontuações por tipo de acerto.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ScoringServiceIntegrationTest {

    // Contador atômico para garantir matchNumbers únicos entre testes
    private static final AtomicInteger MATCH_COUNTER = new AtomicInteger(20000);

    @Autowired
    private ScoringService scoringService;

    @Autowired
    private PredictionRepository predictionRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Cria e persiste uma partida futura de teste (sem resultado ainda).
     * matchNumber gerado automaticamente para garantir unicidade.
     */
    private Match createMatch() {
        int matchNum = MATCH_COUNTER.getAndIncrement();
        Match m = new Match(matchNum, 1, Stage.GROUP, "A",
                "Time A", "Time B", LocalDateTime.now().plusDays(1), "Estádio", "Cidade");
        return matchRepository.save(m);
    }

    /**
     * Cria e persiste uma partida finalizada com o placar fornecido.
     */
    private Match createFinishedMatch(int homeScore, int awayScore) {
        Match m = createMatch();
        m.setHomeScore120(homeScore);
        m.setAwayScore120(awayScore);
        m.setStatus(MatchStatus.FINISHED);
        return matchRepository.save(m);
    }

    /**
     * Cria e persiste um usuário de teste.
     */
    private User createUser(String username) {
        User u = new User(username, username + "@scoring.test",
                passwordEncoder.encode("senha123"), "Scoring " + username, Role.ROLE_USER);
        return userRepository.save(u);
    }

    /**
     * Cria e persiste um palpite diretamente no repositório (sem passar pela validação de bloqueio).
     */
    private Prediction createPrediction(Long userId, Long matchId, int home, int away) {
        Prediction p = new Prediction(userId, matchId, home, away);
        return predictionRepository.save(p);
    }

    // ─── Testes ───────────────────────────────────────────────────────────────

    /**
     * Teste 1: placar exato (2x1 palpitado, 2x1 real) → 6 pontos.
     */
    @Test
    void calculateScores_exactScore() {
        // Arrange
        Match match = createFinishedMatch(2, 1);
        User user = createUser("scoring_exact_" + System.nanoTime());
        createPrediction(user.getId(), match.getId(), 2, 1);

        // Act
        List<Prediction> results = scoringService.calculateScoresForMatch(match.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPointsAwarded()).isEqualTo(6);
    }

    /**
     * Teste 2: acerto do vencedor + gols de 1 equipe.
     * Palpite: 1x0, Real: 3x0 → mesmo vencedor E acertou gols do visitante (ambos 0) → 3 pontos.
     *
     * NOTA: pred(1,0) vs real(3,0):
     *   - Mesmo resultado (mandante venceu): ✓
     *   - Gols do visitante iguais (0==0): ✓  → critério de 3 pts (vencedor + gols de 1 equipe)
     */
    @Test
    void calculateScores_winnerPlusOneTeamGoals() {
        // Arrange
        Match match = createFinishedMatch(3, 0);
        User user = createUser("scoring_result_" + System.nanoTime());
        createPrediction(user.getId(), match.getId(), 1, 0);

        // Act
        List<Prediction> results = scoringService.calculateScoresForMatch(match.getId());

        // Assert — 3 pts: acertou o vencedor E os gols do visitante (0==0)
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPointsAwarded()).isEqualTo(3);
    }

    /**
     * Teste 2b: acerto apenas do resultado (2 pts).
     * Palpite: 2x1, Real: 3x0 → mesmo vencedor (mandante), mas nenhum gol igual → 2 pontos.
     */
    @Test
    void calculateScores_resultOnly() {
        // Arrange
        Match match = createFinishedMatch(3, 0);
        User user = createUser("scoring_result2_" + System.nanoTime());
        createPrediction(user.getId(), match.getId(), 2, 1);

        // Act
        List<Prediction> results = scoringService.calculateScoresForMatch(match.getId());

        // Assert — 2 pts: acertou apenas o resultado (mandante venceu), sem gols iguais
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPointsAwarded()).isEqualTo(2);
    }

    /**
     * Teste 3: nenhum acerto — palpite errou resultado e placar.
     * Palpite: 1x0 (vitória mandante), Real: 0x1 (vitória visitante) → 0 pontos.
     */
    @Test
    void calculateScores_noHit() {
        // Arrange
        Match match = createFinishedMatch(0, 1);
        User user = createUser("scoring_nohit_" + System.nanoTime());
        createPrediction(user.getId(), match.getId(), 1, 0);

        // Act
        List<Prediction> results = scoringService.calculateScoresForMatch(match.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPointsAwarded()).isEqualTo(0);
    }

    /**
     * Teste 4: idempotência — chamar calculateScoresForMatch duas vezes com o mesmo placar
     * deve resultar na mesma pontuação (sem duplicação ou alteração indevida).
     */
    @Test
    void calculateScores_idempotent() {
        // Arrange
        Match match = createFinishedMatch(2, 2);
        User user = createUser("scoring_idempotent_" + System.nanoTime());
        createPrediction(user.getId(), match.getId(), 2, 2);

        // Act – calcula duas vezes com o mesmo placar
        List<Prediction> firstRun = scoringService.calculateScoresForMatch(match.getId());
        List<Prediction> secondRun = scoringService.calculateScoresForMatch(match.getId());

        // Assert – a pontuação deve ser a mesma nas duas execuções
        assertThat(firstRun).hasSize(1);
        assertThat(secondRun).hasSize(1);
        assertThat(firstRun.get(0).getPointsAwarded())
                .isEqualTo(secondRun.get(0).getPointsAwarded())
                .isEqualTo(6); // placar exato 2x2

        // Verifica que existe apenas 1 palpite no banco (não duplicou)
        List<Prediction> inDb = predictionRepository.findByMatchId(match.getId());
        assertThat(inDb).hasSize(1);
    }

    /**
     * Teste 5: recalculateAllScores deve processar apenas partidas com resultado definido
     * (homeScore120 e awayScore120 não-nulos) e ignorar as SCHEDULED sem resultado.
     *
     * O serviço recalculateAllScores filtra por getHomeScore120() != null && getAwayScore120() != null,
     * não pelo status MatchStatus.FINISHED — por isso o "SCHEDULED com resultado" também seria processado.
     * O teste valida que a partida sem resultado (nulos) é ignorada.
     */
    @Test
    void recalculateAll_onlyFinished() {
        // Arrange – partida 1: FINISHED com resultado 2x0
        Match finished = createFinishedMatch(2, 0);

        // Arrange – partida 2: SCHEDULED sem resultado (scores são nulos)
        Match scheduled = createMatch(); // sem homeScore120/awayScore120
        assertThat(scheduled.getHomeScore120()).isNull();
        assertThat(scheduled.getAwayScore120()).isNull();

        // Cria usuários e palpites para ambas as partidas
        User user = createUser("scoring_recalc_" + System.nanoTime());
        Prediction predFinished = createPrediction(user.getId(), finished.getId(), 2, 0);
        Prediction predScheduled = createPrediction(user.getId(), scheduled.getId(), 1, 1);

        // Act
        scoringService.recalculateAllScores();

        // Assert – o palpite da partida FINISHED deve ter pontuação calculada (6 pts, exato)
        Prediction reloadedFinished = predictionRepository.findById(predFinished.getId()).orElseThrow();
        assertThat(reloadedFinished.getPointsAwarded()).isEqualTo(6);

        // Assert – o palpite da partida SCHEDULED (sem placar) NÃO deve ter pontuação calculada
        Prediction reloadedScheduled = predictionRepository.findById(predScheduled.getId()).orElseThrow();
        assertThat(reloadedScheduled.getPointsAwarded()).isNull();
    }
}
