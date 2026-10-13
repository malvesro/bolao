package com.bolao2027.modules.tournament;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes MockMvc para AdminController.
 *
 * Usa @SpringBootTest(webEnvironment = MOCK) + MockMvcBuilders.webAppContextSetup() para ter o
 * contexto completo com H2 in-memory, incluindo os dados semeados pelos seeders (64 partidas, etc.).
 * No Spring Boot 4.x, @AutoConfigureMockMvc foi removido — o MockMvc deve ser construído
 * manualmente via WebApplicationContext no @BeforeEach.
 *
 * AdminController é protegido pelo SecurityConfig que exige ROLE_ADMIN para /admin/**.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class AdminControllerTest {

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
     * Teste 1: GET /admin/matches sem autenticação deve redirecionar para login (302).
     * Spring Security intercepta a requisição anônima e redireciona para /login.
     */
    @Test
    void getAdminMatches_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/matches"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Teste 2: GET /admin/matches com ROLE_USER deve retornar 403 Forbidden.
     * A rota /admin/** exige ROLE_ADMIN — um usuário comum é rejeitado.
     */
    @Test
    void getAdminMatches_roleUser_forbidden() throws Exception {
        mockMvc.perform(get("/admin/matches")
                        .with(user("player").roles("USER")))
                .andExpect(status().isForbidden());
    }

    /**
     * Teste 3: GET /admin/matches com ROLE_ADMIN deve retornar 200.
     * Com H2 populado pelo MatchDataSeeder, a lista real de 64 partidas é carregada — OK.
     */
    @Test
    void getAdminMatches_roleAdmin_returns200() throws Exception {
        mockMvc.perform(get("/admin/matches")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    /**
     * Teste 4: POST /admin/recalculate com ROLE_ADMIN deve redirecionar (302).
     * O método recalculate() executa o recálculo e redireciona para /admin/matches.
     * Com H2 sem partidas finalizadas, o recálculo é uma operação vazia mas válida.
     */
    @Test
    void postRecalculate_roleAdmin_redirects() throws Exception {
        mockMvc.perform(post("/admin/recalculate")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
