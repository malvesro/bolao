package com.bolao2027.modules.prediction;

import com.bolao2027.modules.identity.domain.Role;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.application.PredictionService;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.prediction.domain.PredictionLockedException;
import com.bolao2027.modules.prediction.infrastructure.persistence.PredictionRepository;
import com.bolao2027.modules.tournament.domain.Match;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração para PredictionService usando banco H2 in-memory.
 * Cada teste roda dentro de uma transação que é revertida ao final (rollback automático).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PredictionServiceIntegrationTest {

    // Contador atômico para garantir matchNumbers únicos entre testes
    private static final AtomicInteger MATCH_COUNTER = new AtomicInteger(10000);

    @Autowired
    private PredictionService predictionService;

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
     * Cria e persiste uma partida de teste com o matchNumber gerado automaticamente.
     * O matchNumber começa em 10000 para não conflitar com os 64 da seeder oficial.
     */
    private Match createMatch(LocalDateTime dateTime) {
        int matchNum = MATCH_COUNTER.getAndIncrement();
        Match m = new Match(matchNum, 1, Stage.GROUP, "A",
                "Brasil", "Argentina", dateTime, "Maracanã", "Rio de Janeiro");
        return matchRepository.save(m);
    }

    /**
     * Cria e persiste um usuário de teste com username único.
     */
    private User createUser(String username) {
        User u = new User(username, username + "@test.com",
                passwordEncoder.encode("senha123"), "Usuário " + username, Role.ROLE_USER);
        return userRepository.save(u);
    }

    // ─── Testes ───────────────────────────────────────────────────────────────

    /**
     * Teste 1: salva um palpite para partida futura (não bloqueada) e verifica que foi persistido.
     * A partida começa em 2h, portanto a janela de palpite está aberta (T-1h não passou).
     */
    @Test
    void savePrediction_success() {
        // Arrange
        Match match = createMatch(LocalDateTime.now().plusHours(2));
        User user = createUser("user_success_" + System.nanoTime());

        // Act
        Prediction saved = predictionService.savePrediction(user.getId(), match.getId(), 2, 1);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getMatchId()).isEqualTo(match.getId());
        assertThat(saved.getHomeScore()).isEqualTo(2);
        assertThat(saved.getAwayScore()).isEqualTo(1);

        // Confirma persistência no repositório
        List<Prediction> inDb = predictionRepository.findByMatchId(match.getId());
        assertThat(inDb).hasSize(1);
        assertThat(inDb.get(0).getId()).isEqualTo(saved.getId());
    }

    /**
     * Teste 2: salva um palpite, depois o atualiza com novo placar,
     * e verifica que há apenas 1 registro no banco (upsert/idempotência).
     */
    @Test
    void savePrediction_update() {
        // Arrange
        Match match = createMatch(LocalDateTime.now().plusHours(2));
        User user = createUser("user_update_" + System.nanoTime());

        // Act – primeiro save
        predictionService.savePrediction(user.getId(), match.getId(), 1, 0);

        // Act – update com novo placar
        Prediction updated = predictionService.savePrediction(user.getId(), match.getId(), 3, 2);

        // Assert – apenas 1 palpite no banco para este (usuário, partida)
        List<Prediction> inDb = predictionRepository.findByMatchId(match.getId());
        assertThat(inDb).hasSize(1);
        assertThat(updated.getHomeScore()).isEqualTo(3);
        assertThat(updated.getAwayScore()).isEqualTo(2);
    }

    /**
     * Teste 3: tenta salvar palpite para partida que já passou do tempo de bloqueio (T-1h).
     * A partida começa em 30 minutos no passado, portanto está bloqueada.
     * Espera-se PredictionLockedException.
     */
    @Test
    void savePrediction_locked_throws() {
        // Arrange – partida já bloqueada (começou 30min atrás, lockout é T-1h, portanto now > matchTime - 1h)
        Match match = createMatch(LocalDateTime.now().minusMinutes(30));
        User user = createUser("user_locked_" + System.nanoTime());

        // Act & Assert
        assertThatThrownBy(() ->
                predictionService.savePrediction(user.getId(), match.getId(), 1, 0)
        ).isInstanceOf(PredictionLockedException.class);
    }

    /**
     * Teste 4: para uma partida futura (não bloqueada), getPublicPredictionsForMatch
     * deve retornar lista vazia, mesmo que haja palpite salvo (HU-20: visibilidade pós-bloqueio).
     */
    @Test
    void getPublicPredictions_beforeLock_empty() {
        // Arrange – partida ainda não bloqueada (começa em 2h)
        Match match = createMatch(LocalDateTime.now().plusHours(2));
        User user = createUser("user_beforelock_" + System.nanoTime());
        predictionService.savePrediction(user.getId(), match.getId(), 2, 2);

        // Act
        List<Prediction> public_ = predictionService.getPublicPredictionsForMatch(match.getId());

        // Assert – vazio porque a partida ainda não foi bloqueada
        assertThat(public_).isEmpty();
    }

    /**
     * Teste 5: para uma partida já bloqueada (passado do tempo de corte),
     * getPublicPredictionsForMatch deve retornar os palpites existentes.
     * Palpite é inserido diretamente no repositório para contornar a validação de bloqueio
     * do serviço (que impediria de salvar via savePrediction).
     */
    @Test
    void getPublicPredictions_afterLock_visible() {
        // Arrange – partida bloqueada (matchDateTime = 30min atrás, portanto now > matchDateTime - 1h)
        Match match = createMatch(LocalDateTime.now().minusMinutes(30));
        User user = createUser("user_afterlock_" + System.nanoTime());

        // Insere diretamente no repositório (bypass do serviço para evitar PredictionLockedException)
        Prediction p = new Prediction(user.getId(), match.getId(), 1, 0);
        predictionRepository.save(p);

        // Act
        List<Prediction> public_ = predictionService.getPublicPredictionsForMatch(match.getId());

        // Assert – deve retornar o palpite pois a partida está bloqueada
        assertThat(public_).hasSize(1);
        assertThat(public_.get(0).getUserId()).isEqualTo(user.getId());
    }
}
