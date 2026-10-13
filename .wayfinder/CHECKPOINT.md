# Checkpoint de Sessão — Bolão Copa do Mundo Feminina FIFA Brasil 2027™

**Timestamp:** 18/09/2026 — 22:20 BRT  
**Status do Projeto:** ✅ BUILD SUCCESS — 53 testes passando (Java 25 + Spring Boot 4.1.1)  
**Sprint concluída:** Sprint 3 — Email, Profile, UX/Countdown, Mata-Mata, Integração  
**Metodologia:** Monolito Modular DDD + Clean Architecture + Gauntlet Loop & Wayfinder  
**Stack:** Java 25 · Spring Boot 4.1.1 · Thymeleaf · HTMX 2.0.4 · PostgreSQL 17 · Docker Compose  

---

## 1. Estado Atual — Tudo Implementado e Validado

### Módulo `tournament`
- `Match.java` — entidade completa: 120min, pênaltis, status, `isLocked()`, `getCutoffEpoch()`
- `MatchStatus.java`, `Stage.java` (valores: GROUP, ROUND_OF_16, QUARTER_FINALS, SEMI_FINALS, THIRD_PLACE, FINAL)
- `MatchService.java` — findAll, findByRound, findByStage, updateMatchResult, updateKnockoutTeams
- `KnockoutAdvanceService.java` — `advanceGroupsToRoundOf16()`, `advanceKnockoutWinners()`
- `MatchController.java` — GET/POST /matches e fragmentos HTMX
- `AdminController.java` — /admin/matches, /admin/recalculate, /admin/knockout (advance-groups, advance-winners)
- `MatchDataSeeder.java` — 64 partidas oficiais
- `MatchRepository.java`

### Módulo `identity`
- `User.java`, `Role.java`, `PasswordResetToken.java`
- `UserService.java` — registro, troca de senha, `createPasswordResetToken`, `resetPasswordWithToken`
- `EmailService.java` — envio de email HTML dark-theme via Spring Mail (graceful degradation sem SMTP)
- `SecurityConfig.java` — rotas públicas + ROLE_ADMIN
- `CustomUserDetailsService.java`, `UserDataSeeder.java`
- `IdentityController.java` — GET/POST /register, GET/POST /forgot-password, GET/POST /reset-password
- `ProfileController.java` — GET /profile (histórico por rodada, stats, ranking)
- `ProfileStats.java` — record DTO
- `HomeController.java` — GET / → redirect:/matches, GET /login
- `UserRepository.java`, `PasswordResetTokenRepository.java`

### Módulo `prediction`
- `Prediction.java`, `PredictionLockedException.java`, `PredictionRepository.java`
- `PredictionService.java` — savePrediction (trava T-1h), getPublicPredictionViewsForMatch, getUserPredictions
- `PredictionView.java` — record DTO com displayName

### Módulo `scoring`
- `ScoringEngine.java` — motor 6/3/2/1/0, regra 120min
- `ScoreCalculationResult.java`, `ScoringService.java`

### Módulo `ranking`
- `RankingEntry.java`, `RankingTieBreakerComparator.java` (5 critérios)
- `RankingService.java` — buildLeaderboard(), buildLeaderboardUpToRound(), getUserRanking()
- `LeaderboardController.java` — /leaderboard + fragment HTMX polling 60s
- `RankingRepository.java`

### Infraestrutura compartilhada
- `DateUtils.java` — nowBrt, isMatchLocked, cutoffEpoch, minutesUntilCutoff
- `HomeController.java`

### Templates Thymeleaf + HTMX (14 templates)
- `layout/base.html` — dark theme, navbar (Partidas, Ranking, 👤 Meu Perfil, Admin), countdown JS, toasts
- `matches/list.html` — grid por rodada + onboarding de regras colapsável + countdown nas cards
- `matches/detail.html`, `matches/fragments.html`
- `leaderboard/index.html`, `leaderboard/fragments.html`
- `admin/matches.html`, `admin/fragments.html`, `admin/knockout.html`
- `auth/login.html`, `auth/register.html`, `auth/forgot-password.html`, `auth/reset-password.html`
- `profile/index.html` — badges de performance + histórico por rodada (details/summary nativo)

### Testes (53 testes, BUILD SUCCESS)
| Suite | Tipo | Qtd |
|-------|------|-----|
| `ScoringEngineTest` | Unitário | 23 |
| `RankingTieBreakerTest` | Unitário | 14 |
| `PredictionServiceIntegrationTest` | Integração | 5 |
| `ScoringServiceIntegrationTest` | Integração | 6 |
| `RankingServiceIntegrationTest` | Integração | 4 |
| `Bolao2027ApplicationTests` | Smoke | 1 |
| **Total** | | **53** |

### Docker
- `Dockerfile` — multi-stage eclipse-temurin:25
- `docker-compose.yml` — postgres:17-alpine + healthcheck
- `.dockerignore`

---

## 2. Próximos Passos — Sprint 4 (Backlog priorizado)

| Prioridade | Item | HUs |
|-----------|------|-----|
| 🔴 Alta | Envio real de e-mail SMTP (Mailtrap/Mailhog p/ dev, variável p/ prod) | HU-06 |
| 🔴 Alta | Flyway migrations em vez de `ddl-auto: update` | Ticket 008 |
| 🟡 Média | Paginação e filtro por rodada no leaderboard | HU-10 |
| 🟡 Média | Gamificação: badges persistidos no banco, histórico rodada-a-rodada | HU-21 |
| 🟢 Baixa | Canal de suporte/reporte ao administrador | HU-22 |
| 🟢 Baixa | Testes de controller com MockMvc (testes de UI) | — |

---

## 3. Prompt de Retomada Rápida

```
Continuar a implementação do monolito Bolão Copa do Mundo Feminina 2027 considerando o
checkpoint em .wayfinder/CHECKPOINT.md com a skill gauntlet-loop em paralelo com multiagentes.
```

---

## 4. Comandos Úteis

```bash
# Compilar e rodar todos os 53 testes
./mvnw test

# Só testes unitários (rápido)
./mvnw test -Dtest="ScoringEngineTest,RankingTieBreakerTest"

# Só testes de integração
./mvnw test -Dtest="PredictionServiceIntegrationTest,ScoringServiceIntegrationTest,RankingServiceIntegrationTest"

# Subir localmente (H2 in-memory)
./mvnw spring-boot:run

# Subir com Docker + PostgreSQL 17
docker compose up --build

# H2 console (dev): http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:bolao2027
```
