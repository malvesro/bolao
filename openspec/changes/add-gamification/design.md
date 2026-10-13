# Design

## Context

O módulo `gamification` já existe em casca: `BadgeType` enum com 7 badges, `UserBadge`, `BadgeService.evaluateAndAward` idempotente e `UserBadgeRepository`. O `RankingService` calcula posições em tempo real mas não mantém snapshot histórico por rodada — o que torna impossível saber quem estava em 1º ao final da Rodada 3, por exemplo, para o badge LIDER_RODADA.

O perfil do participante (`/profile`) hoje mostra histórico por rodada de pontos, mas não gráfico de evolução de posição no ranking nem exibição de badges conquistados. O Ranking já tem `buildLeaderboardUpToRound(maxRound)` que permite posição retrospectiva, mas executado sob demanda tem custo (recalcula para todos os usuários).

## Goals / Non-Goals

**Goals:**
- Exibir badges conquistados no perfil do participante
- Implementar badges que dependem de posição em rodada (LIDER_RODADA, COMEBACK) de forma confiável
- Histórico de posição/pontos por rodada com gráfico simples acessível (HTMX + SVG ou canvas mínimo)

**Non-Goals:**
- Não persistir shells/leagues de usuario
- Não criar novos endpoints REST API explícitos — os dados fluem nos templates Thymeleaf existentes
- Sistema de ranking em tempo real com WebSockets (atualização de posição permanece por polling HTMX)

## Proposed Changes

### gamification — BadgeService

**O que já existe:** `evaluateAndAward(userId)` avalia 5 badges com base apenas em Prediction (contagem de exatos). Faltam badges que dependem do ranking (LIDER_RODADA, COMEBACK). Badge LIDER_RODADA requer conhecer posição ao final de uma rodada completa.

**O que muda:**
1. Adicionar snapshots de ranking: nova entidade `RankingSnapshot` (userId, roundNumber, position, pointsAtRound) gravada no evento de uma rodada ser finalizada (match.status → FINISHED e todos os jogos da rodada com status FINISHED). Listener no ScoringService ou MatchService.
2. BadgeService ganha `evaluateRoundEnd(userId, roundNumber)` chamado quando uma rodada é completada:
   - LIDER_RODADA: posição == 1 no snapshot
   - COMEBACK: snapshot.position(round) - snapshot.position(round-1) >= 5 (subiu 5+)
3. PerfilController passa `List<UserBadge>` ao template `profile/index.html`

### profile — Gráfico de Evolução

**O que já existe:** `ProfileStats` tem pontos por rodada, não posição.

**O que muda:**
- `ProfileStats` ganha `List<RoundSnapshot>` com position + points por rodada apurada
- Template `profile/index.html` ganha seção "Evolução" com gráfico SVG gerado via JS simples (sem biblioteca) ou Chart.js se necessário
- Endpoint `/profile` já existe; só passa mais dados ao model

## Decisions

### RankingSnapshot vs cálculo ao-vivo

Alternative 1: Calcular `RankingService.buildLeaderboardUpToRound(round)` para cada rodada a cada vez que precisar — simples mas O(n * rodadas) a cada perfil visto e ao avaliar badges de todos os usuários em uma rodada.

Alternative 2 (escolhida): Persistir `RankingSnapshot` quando uma rodada é finalizada. Custo: 1 write por usuário por rodada. Leituras de histórico tornam-se O(rodadas).

Razão: BadgeService precisa de snapshot para LIDER_RODADA/COMEBACK; se calcular ao-vivo, cada avaliação em lote seria cara. A avaliação acontece de uma vez por rodada.

### Badge avaliado em qual evento?

Os badges baseados em Prediction (contagem de exatos) são avaliados sincronamente em `PredictionService.savePrediction`. Badges baseados em posição em ranking só podem ser avaliados quando uma rodada é "finalizada" (todos os jogos com status FINISHED). Evento: `RoundCompletedEvent` disparado pelo MatchService após `updateMatchResult` marcar o último jogo da rodada como FINISHED.

### Frontend do gráfico

Valor minimalista: canvas com `<canvas>` + JS custom desenhando barras/linha por rodada, sem dependência npm. O Thymeleaf entrega os dados via `th:inline="javascript"`.

Se complexidade crescer (interatividade hover, tooltips), migrar para Chart.js v4 via CDN com graceful degradation.

## Risks / Trade-offs

| Risco | Mitigação |
|-------|-----------|
| RankingSnapshot duplicado se a rodada for re-finalizada | Constraint UNIQUE(userId, roundNumber) + saveOrUpdate |
| Badge concedido antes da rodada ser finalizada (rodada "parcial") | `evaluateRoundEnd` só roda após RoundCompletedEvent; rodada parcial não dispara evento |
| Performance: calcular snapshot ao finalizar rodada custa O(usuários) | Ocupacional apenas no evento de conclusão de rodada; executado assincronamente ou em thread dedicada |

## Open Questions

- O gráfico de evolução mostra posição desde o início (Rodada 0 → todos posição 1 com 0 pts) ou só a partir da primeira rodada apurada? Default: rodada 0 implícita com position=1, pts=0.