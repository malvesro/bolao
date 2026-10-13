package com.bolao2027.modules.ranking;

import com.bolao2027.modules.identity.domain.Role;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import com.bolao2027.modules.ranking.application.RankingService;
import com.bolao2027.modules.ranking.domain.RankingEntry;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.MatchStatus;
import com.bolao2027.modules.tournament.domain.Stage;
import com.bolao2027.modules.tournament.infrastructure.persistence.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para RankingService usando banco H2 in-memory.
 *
 * NOTA: Como o RankingService.buildLeaderboard() busca TODOS os usuários e palpites pontuados
 * do banco, e os seeders (UserDataSeeder + MatchDataSeeder) podem inserir dados ao iniciar
 * o contexto Spring, usamos @DirtiesContext para garantir um contexto limpo por teste,
 * evitando interferência de dados residuais dos seeders ou de outros testes.
 *
 * Alternativa: os seeders só inserem dados quando count() == 0, mas com create-drop no H2
 * o banco é recriado para cada contexto. Com @DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
 * garantimos isolamento total.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RankingServiceIntegrationTest {

    private static final AtomicInteger MATCH_COUNTER = new AtomicInteger(30000);

    @Autowired
    private RankingService rankingService;

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
     * Cria e persiste uma partida finalizada com resultado.
     */
    private Match createFinishedMatch(int homeScore, int awayScore) {
        int matchNum = MATCH_COUNTER.getAndIncrement();
        Match m = new Match(matchNum, 1, Stage.GROUP, "A",
                "Time A", "Time B", LocalDateTime.now().minusDays(1), "Estádio", "Cidade");
        m.setHomeScore120(homeScore);
        m.setAwayScore120(awayScore);
        m.setStatus(MatchStatus.FINISHED);
        return matchRepository.save(m);
    }

    /**
     * Cria e persiste um usuário de teste.
     */
    private User createUser(String displayName, String username) {
        User u = new User(username, username + "@ranking.test",
                passwordEncoder.encode("senha123"), displayName, Role.ROLE_USER);
        return userRepository.save(u);
    }

    /**
     * Cria um palpite já pontuado e o persiste diretamente no repositório.
     *
     * @param userId   ID do usuário
     * @param matchId  ID da partida
     * @param home     Placar palpitado do mandante
     * @param away     Placar palpitado do visitante
     * @param points   Pontuação a ser atribuída
     * @param reason   Razão da pontuação
     */
    private Prediction createScoredPrediction(Long userId, Long matchId,
                                               int home, int away,
                                               int points, String reason) {
        Prediction p = new Prediction(userId, matchId, home, away);
        p.applyScore(points, reason);
        return predictionRepository.save(p);
    }

    // ─── Testes ───────────────────────────────────────────────────────────────

    /**
     * Teste 1: sem nenhum palpite pontuado no banco, o leaderboard deve ser vazio.
     * Os seeders inserem usuários, mas sem palpites pontuados nenhum entra no ranking.
     */
    @Test
    void buildLeaderboard_empty() {
        // Act – nenhum palpite foi inserido ou pontuado
        List<RankingEntry> leaderboard = rankingService.buildLeaderboard();

        // Assert – leaderboard deve estar vazio
        assertThat(leaderboard).isEmpty();
    }

    /**
     * Teste 2: 1 usuário com 1 palpite pontuado (6 pts) → leaderboard com 1 entrada na posição 1.
     */
    @Test
    void buildLeaderboard_singleUser() {
        // Arrange
        Match match = createFinishedMatch(2, 1);
        User user = createUser("Jogador Solo", "solo_" + System.nanoTime());
        createScoredPrediction(user.getId(), match.getId(), 2, 1, 6, "Placar exato");

        // Act
        List<RankingEntry> leaderboard = rankingService.buildLeaderboard();

        // Assert – exatamente 1 entrada no ranking
        // Filtra pelo userId do usuário criado neste teste (pode haver seeders)
        List<RankingEntry> userEntries = leaderboard.stream()
                .filter(e -> e.userId().equals(user.getId()))
                .toList();

        assertThat(userEntries).hasSize(1);
        RankingEntry entry = userEntries.get(0);
        assertThat(entry.totalPoints()).isEqualTo(6);
        assertThat(entry.exactScoreCount()).isEqualTo(1);
        assertThat(entry.position()).isEqualTo(1);
    }

    /**
     * Teste 3: 2 usuários com pontuações diferentes (6 pts e 2 pts) → ordenação decrescente.
     */
    @Test
    void buildLeaderboard_orderedByPoints() {
        // Arrange
        Match match1 = createFinishedMatch(2, 1);
        Match match2 = createFinishedMatch(1, 0);

        long suffix = System.nanoTime();
        User userA = createUser("Usuário Alpha", "alpha_" + suffix);
        User userB = createUser("Usuário Beta", "beta_" + (suffix + 1));

        // userA: placar exato → 6 pts
        createScoredPrediction(userA.getId(), match1.getId(), 2, 1, 6, "Placar exato");
        // userB: apenas resultado → 2 pts
        createScoredPrediction(userB.getId(), match2.getId(), 3, 0, 2, "Apenas resultado");

        // Act
        List<RankingEntry> leaderboard = rankingService.buildLeaderboard();

        // Assert – extrair apenas as entradas dos usuários criados neste teste
        List<RankingEntry> entries = leaderboard.stream()
                .filter(e -> e.userId().equals(userA.getId()) || e.userId().equals(userB.getId()))
                .toList();

        assertThat(entries).hasSizeGreaterThanOrEqualTo(2);

        // O primeiro dos dois deve ser userA (6 pts > 2 pts)
        int posA = leaderboard.stream()
                .filter(e -> e.userId().equals(userA.getId()))
                .findFirst().orElseThrow().position();
        int posB = leaderboard.stream()
                .filter(e -> e.userId().equals(userB.getId()))
                .findFirst().orElseThrow().position();

        assertThat(posA).isLessThan(posB);
    }

    /**
     * Teste 4: 2 usuários com mesma pontuação total (6 pts cada), mas user A tem 1 acerto exato
     * e user B tem 0 acertos exatos (3+3=6) → user A deve aparecer ANTES na lista ordenada.
     *
     * NOTA sobre posição numérica: como ambos têm totalPoints == 6, isTiedWith() retorna true
     * e assignPositions() atribui a MESMA posição numérica a ambos (ex: posição 1 para os dois).
     * O critério de desempate (exatos) determina a ORDEM na lista, não a posição numérica.
     * Por isso verificamos o índice na lista, não o campo position().
     *
     * userA: 1 palpite exato (6 pts, exactScoreCount=1)
     * userB: 2 palpites parciais (3+3=6 pts, exactScoreCount=0)
     */
    @Test
    void buildLeaderboard_tieBreakByExact() {
        // Arrange – 3 partidas para este teste
        Match match1 = createFinishedMatch(2, 1); // userA acerta exato
        Match match2 = createFinishedMatch(3, 0); // userB acerta parcial (vencedor + gols)
        Match match3 = createFinishedMatch(1, 1); // userB acerta parcial (empate com gol)

        long suffix = System.nanoTime();
        User userA = createUser("Desempate Alpha", "tieA_" + suffix);
        User userB = createUser("Desempate Beta", "tieB_" + (suffix + 1));

        // userA: 1 acerto exato → 6 pts (exactScoreCount = 1)
        createScoredPrediction(userA.getId(), match1.getId(), 2, 1, 6, "Placar exato");

        // userB: 2 acertos parciais → 3+3 = 6 pts (exactScoreCount = 0, partialScoreCount = 2)
        createScoredPrediction(userB.getId(), match2.getId(), 3, 0, 3, "Vencedor + gols de uma equipe");
        createScoredPrediction(userB.getId(), match3.getId(), 2, 1, 3, "Vencedor + gols de uma equipe");

        // Act
        List<RankingEntry> leaderboard = rankingService.buildLeaderboard();

        // Assert – ambos com 6 pts, mas userA tem 1 exato e userB tem 0 exatos
        RankingEntry entryA = leaderboard.stream()
                .filter(e -> e.userId().equals(userA.getId()))
                .findFirst().orElseThrow();
        RankingEntry entryB = leaderboard.stream()
                .filter(e -> e.userId().equals(userB.getId()))
                .findFirst().orElseThrow();

        assertThat(entryA.totalPoints()).isEqualTo(6);
        assertThat(entryB.totalPoints()).isEqualTo(6);
        assertThat(entryA.exactScoreCount()).isEqualTo(1);
        assertThat(entryB.exactScoreCount()).isEqualTo(0);

        // Ambos recebem a MESMA posição numérica por estarem empatados em totalPoints.
        // O desempate é pela ordem na lista: userA (1 exato) deve vir ANTES de userB (0 exatos).
        int indexA = leaderboard.indexOf(entryA);
        int indexB = leaderboard.indexOf(entryB);
        assertThat(indexA).isLessThan(indexB);
    }
}
