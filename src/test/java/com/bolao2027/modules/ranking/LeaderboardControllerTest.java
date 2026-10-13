package com.bolao2027.modules.ranking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes MockMvc para LeaderboardController.
 *
 * Usa @SpringBootTest(webEnvironment = MOCK) + MockMvcBuilders.webAppContextSetup() para ter
 * acesso ao contexto completo com H2 in-memory e todos os beans reais (incluindo RankingService).
 * No Spring Boot 4.x, @AutoConfigureMockMvc foi removido — o MockMvc deve ser construído
 * manualmente via WebApplicationContext no @BeforeEach.
 *
 * /leaderboard é uma rota pública (SecurityConfig.permitAll) — não requer autenticação.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class LeaderboardControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ─── Testes ───────────────────────────────────────────────────────────────

    /**
     * Teste 1: GET /leaderboard deve retornar 200 e conter a palavra "Ranking" na página.
     */
    @Test
    void getLeaderboard_returns200WithRankingContent() throws Exception {
        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ranking")));
    }

    /**
     * Teste 2: GET /leaderboard?round=1 deve retornar 200 (filtro por rodada).
     */
    @Test
    void getLeaderboard_withRoundFilter_returns200() throws Exception {
        mockMvc.perform(get("/leaderboard").param("round", "1"))
                .andExpect(status().isOk());
    }

    /**
     * Teste 3: GET /leaderboard?page=0&size=5 deve retornar 200 (paginação).
     */
    @Test
    void getLeaderboard_withPagination_returns200() throws Exception {
        mockMvc.perform(get("/leaderboard")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    /**
     * Teste 4: GET /leaderboard/fragment deve retornar 200 (fragmento HTMX).
     * Rota /leaderboard/fragment requer autenticação (não está no permitAll).
     * Usa .with(user(...)) para simular autenticação.
     */
    @Test
    void getLeaderboardFragment_returns200() throws Exception {
        mockMvc.perform(get("/leaderboard/fragment")
                        .with(user("player").roles("USER")))
                .andExpect(status().isOk());
    }
}
