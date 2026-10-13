package com.bolao2027.modules.tournament;

import com.bolao2027.modules.identity.domain.Role;
import com.bolao2027.modules.identity.domain.User;
import com.bolao2027.modules.identity.infrastructure.persistence.UserRepository;
import com.bolao2027.modules.prediction.application.PredictionService;
import com.bolao2027.modules.prediction.domain.Prediction;
import com.bolao2027.modules.tournament.application.MatchService;
import com.bolao2027.modules.tournament.domain.Match;
import com.bolao2027.modules.tournament.domain.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Testes MockMvc para MatchController.
 *
 * Usa @SpringBootTest(webEnvironment = MOCK) + MockMvcBuilders.webAppContextSetup() para ter
 * o contexto completo (Thymeleaf, Security, etc.) enquanto ainda permite mockar os serviços.
 * No Spring Boot 4.x, @AutoConfigureMockMvc foi removido — o MockMvc deve ser construído
 * manualmente via WebApplicationContext no @BeforeEach.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class MatchControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private PredictionService predictionService;

    @MockitoBean
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Cria um Match via Mockito.mock() para ter controle total sobre os getters,
     * incluindo id que normalmente só é preenchido pelo banco (@GeneratedValue).
     */
    private Match createMockMatch(Long id, boolean locked) {
        Match m = mock(Match.class);
        when(m.getId()).thenReturn(id);
        when(m.getMatchNumber()).thenReturn(1);
        when(m.getRoundNumber()).thenReturn(1);
        when(m.getStage()).thenReturn(Stage.GROUP);
        when(m.getGroupLetter()).thenReturn("A");
        when(m.getHomeTeam()).thenReturn("Brasil");
        when(m.getAwayTeam()).thenReturn("Argentina");
        when(m.getStadium()).thenReturn("Maracanã");
        when(m.getCity()).thenReturn("Rio de Janeiro");
        when(m.isLocked()).thenReturn(locked);
        when(m.isFinished()).thenReturn(false);
        when(m.isTeamsTbd()).thenReturn(false);
        when(m.getHomeScore120()).thenReturn(null);
        when(m.getAwayScore120()).thenReturn(null);
        when(m.getPenaltyWinner()).thenReturn(null);
        when(m.getStatus()).thenReturn(com.bolao2027.modules.tournament.domain.MatchStatus.SCHEDULED);
        LocalDateTime dt = locked
                ? LocalDateTime.now().minusMinutes(30)
                : LocalDateTime.now().plusHours(2);
        when(m.getMatchDateTime()).thenReturn(dt);
        when(m.getFormattedDateTime()).thenReturn("01/08/2027 15:00");
        when(m.getFormattedDate()).thenReturn("01/08/2027");
        when(m.getFormattedTime()).thenReturn("15:00");
        when(m.getFormattedCutoffTime()).thenReturn("01/08/2027 14:00");
        when(m.getMinutesUntilCutoff()).thenReturn(locked ? -30L : 60L);
        when(m.getCutoffEpoch()).thenReturn(0L);
        return m;
    }

    // ─── Testes ───────────────────────────────────────────────────────────────

    /**
     * Teste 1: GET /matches sem autenticação deve retornar 200.
     * A rota /matches é pública (SecurityConfig.permitAll).
     */
    @Test
    void getMatches_unauthenticated_returns200() throws Exception {
        when(matchService.findAllMatches()).thenReturn(List.of());

        mockMvc.perform(get("/matches"))
                .andExpect(status().isOk());
    }

    /**
     * Teste 2: GET /matches/{id} com match existente deve retornar 200.
     */
    @Test
    void getMatchDetail_found_returns200() throws Exception {
        Match mockMatch = createMockMatch(1L, false);
        when(matchService.findById(1L)).thenReturn(Optional.of(mockMatch));
        when(predictionService.getOrCreatePrediction(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/matches/1"))
                .andExpect(status().isOk());
    }

    /**
     * Teste 3: GET /matches/{id} com match não encontrado deve lançar exceção.
     * A controller lança IllegalArgumentException que resulta em erro (4xx ou 5xx).
     */
    @Test
    void getMatchDetail_notFound_returnsError() throws Exception {
        when(matchService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/matches/99"))
                .andExpect(status().is5xxServerError());  // IllegalArgumentException → 500
    }

    /**
     * Teste 4: POST /matches/{id}/predict sem autenticação e sem CSRF token.
     * Spring Security rejeita com 403 (CSRF token inválido) antes de verificar autenticação.
     * A rota /matches/* está em permitAll, mas o filtro CSRF processa antes.
     */
    @Test
    void postPredict_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/matches/1/predict")
                        .param("homeScore", "1")
                        .param("awayScore", "0"))
                .andExpect(status().isForbidden());  // 403: CSRF token missing
    }

    /**
     * Teste 5: POST /matches/{id}/predict com usuário autenticado deve retornar 200 (fragmento).
     * Usa .with(user(...)) para simular autenticação.
     */
    @Test
    void postPredict_authenticated_returns200() throws Exception {
        Match mockMatch = createMockMatch(1L, false);
        User mockUser = new User("testuser", "testuser@test.com", "pass", "Test User", Role.ROLE_USER);

        when(matchService.findById(1L)).thenReturn(Optional.of(mockMatch));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(predictionService.savePrediction(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(new Prediction(1L, 1L, 2, 1));

        mockMvc.perform(post("/matches/1/predict")
                        .with(user("testuser").roles("USER"))
                        .with(csrf())
                        .param("homeScore", "2")
                        .param("awayScore", "1"))
                .andExpect(status().isOk());
    }
}
