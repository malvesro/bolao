# Gauntlet Loop — Bolão Copa do Mundo Feminina FIFA Brasil 2027™

**Status:** Sprint 3 Concluída ✅  
**Timestamp:** 18/09/2026 — 22:20 BRT  
**Stack:** Java 25 + Spring Boot 4.1.1 + Thymeleaf + HTMX + PostgreSQL + Docker Compose  

---

## 📊 Matriz de Peças — Status Acumulado

| # | Peça | Builder | Critic | Sprint | Veredito |
|---|------|---------|--------|--------|----------|
| 1 | Docker & PostgreSQL | Dockerfile multi-stage + docker-compose.yml postgres:17 | Estrutura verificada | Sprint 2 | ✅ Aprovado |
| 2 | Scoring Engine & 120 min | ScoringEngine + ScoringService | 23 testes unitários | Sprint 1 | ✅ Aprovado |
| 3 | Ranking & 5 Desempates | RankingTieBreakerComparator + RankingService + LeaderboardController | 14 testes unitários | Sprint 2 | ✅ Aprovado |
| 4 | Palpites, Trava T-1h & Transparência | PredictionService + PredictionView DTO | Bloqueio T-1h validado | Sprint 2 | ✅ Aprovado |
| 5 | UI Clean — Thymeleaf + HTMX (base) | Templates: matches, leaderboard, admin, auth | Compilação limpa | Sprint 2 | ✅ Aprovado |
| 6 | Painel Administrativo | AdminController + admin/matches.html HTMX | Aciona ScoringService | Sprint 2 | ✅ Aprovado |
| 7 | Autenticação & Registro | IdentityController + login.html + register.html | SecurityConfig validada | Sprint 2 | ✅ Aprovado |
| 8 | Recuperação de Senha (Email) | EmailService + templates forgot/reset + Spring Mail | Degradação graciosa sem SMTP | Sprint 3 | ✅ Aprovado |
| 9 | Página /profile | ProfileController + ProfileStats + profile/index.html | Histórico por rodada | Sprint 3 | ✅ Aprovado |
| 10 | Countdown + UX + Onboarding | JS countdown data-cutoff-epoch + Toast + regras colapsáveis | getCutoffEpoch() em Match | Sprint 3 | ✅ Aprovado |
| 11 | Avanço Mata-Mata | KnockoutAdvanceService + admin/knockout.html | Propaga vencedores e grupos | Sprint 3 | ✅ Aprovado |
| 12 | Testes de Integração | PredictionServiceIntegrationTest + ScoringServiceIntegrationTest + RankingServiceIntegrationTest | 15 testes, BUILD SUCCESS | Sprint 3 | ✅ Aprovado |

---

## 🧪 Cobertura de Testes — 53 Testes, BUILD SUCCESS ✅

| Suite | Tipo | Testes | Status |
|-------|------|--------|--------|
| `ScoringEngineTest` | Unitário | 23 | ✅ PASS |
| `RankingTieBreakerTest` | Unitário | 14 | ✅ PASS |
| `PredictionServiceIntegrationTest` | Integração | 5 | ✅ PASS |
| `ScoringServiceIntegrationTest` | Integração | 6 | ✅ PASS |
| `RankingServiceIntegrationTest` | Integração | 4 | ✅ PASS |
| `Bolao2027ApplicationTests` | Smoke | 1 | ✅ PASS |
| **Total** | | **53** | **✅ BUILD SUCCESS** |

---

## 🐛 Correções feitas no Agent F (Sprint 3)

1. **`ScoringServiceIntegrationTest.calculateScores_resultOnly`** — pred(1,0) vs real(3,0) retorna 3 pts (não 2), porque acerta o visitante=0. Teste corrigido com caso de 3 pts + novo caso explícito de 2 pts (pred(2,1) vs real(3,0)).

2. **`RankingServiceIntegrationTest.buildLeaderboard_tieBreakByExact`** — em empate de `totalPoints`, a `position` numérica é compartilhada (ambos ficam na posição 1). O desempate por exatos determina a **ordem na lista**, não a posição. Teste corrigido para comparar `indexOf()` em vez de `position()`.

---

## 📁 Arquivos por Sprint

### Sprint 1 — Motor de Pontuação
`ScoringEngine.java`, `ScoreCalculationResult.java`, `ScoringService.java`

### Sprint 2 — UI + Ranking + Docker + Auth (37 testes)
`RankingEntry.java`, `RankingTieBreakerComparator.java`, `RankingService.java`, `LeaderboardController.java`  
`PredictionView.java`, `IdentityController.java`  
Templates: leaderboard, admin, auth, matches  
`Dockerfile`, `docker-compose.yml`, `.dockerignore`, `pom.xml` corrigido

### Sprint 3 — Email + Profile + UX + Mata-Mata + Testes Integração (53 testes)
`EmailService.java`, `ProfileController.java`, `ProfileStats.java`  
`KnockoutAdvanceService.java`, `Match.getCutoffEpoch()`, `DateUtils.cutoffEpoch()`  
Templates: `auth/forgot-password.html`, `auth/reset-password.html`, `profile/index.html`, `admin/knockout.html`  
`layout/base.html` atualizado (countdown JS, toasts, link /profile)  
`matches/list.html` atualizado (onboarding + countdown)  
`src/test/resources/application-test.yml`  
`PredictionServiceIntegrationTest.java`, `ScoringServiceIntegrationTest.java`, `RankingServiceIntegrationTest.java`

---

## 🔄 Sprint 4 — Backlog Priorizado

| Prioridade | Item |
|-----------|------|
| 🔴 Alta | Flyway migrations (substituir ddl-auto: update) |
| 🔴 Alta | Configuração real de SMTP (Mailhog no docker-compose p/ dev) |
| 🟡 Média | Paginação + filtro por rodada no leaderboard |
| 🟡 Média | Gamificação: badges persistidos no banco |
| 🟢 Baixa | MockMvc tests para controllers web |
| 🟢 Baixa | Canal de suporte/reporte ao administrador |

---

## 📎 Referências

- Requisitos: [`.wayfinder/requisitos-bolao-copa-mundo-feminina-2027.md`]
- Mapa: [`.wayfinder/map.md`]
- Checkpoint: [`.wayfinder/CHECKPOINT.md`]
