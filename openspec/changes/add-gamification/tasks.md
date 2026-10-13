# Tasks

## 1. Persistência de Histórico de Ranking

- [ ] 1.1 Criar entidade `RankingSnapshot` (userId, roundNumber, position, pointsAtRound, earnedAt) com tabela `ranking_snapshots` via migração Flyway, verificando que a migração roda com sucesso (`./mvnw flyway:migrate` ou boot com H2)
- [ ] 1.2 Criar `RankingSnapshotRepository` com métodos `findByUserIdOrderByRoundNumberAsc` e `findByRoundNumber`, verificando que os métodos existem e compilam
- [ ] 1.3 Criar `SnapshotService.takeSnapshot(roundNumber)` que calcula e persiste o ranking ao final de uma rodada completa (usando `RankingService.buildLeaderboardUpToRound`), verificando que o método retorna lista de snapshots no tamanho esperado em teste unitário

## 2. Evento de Rodada Concluída

- [ ] 2.1 Criar `RoundCompletedEvent` (record com roundNumber) e publicar via `ApplicationEventPublisher` em `MatchService` quando todas as partidas de uma rodada estiverem `FINISHED`, verificando que o evento é disparado em teste unitário com mock do publisher
- [ ] 2.2 Criar `RoundCompletedListener` no módulo `gamification` que chama `SnapshotService.takeSnapshot(round)` e depois `BadgeService.evaluateRoundEnd(userId, round)` para todos os participantes, verificando com teste de integração que badges são gravados após evento

## 3. Badges de Ranking (LIDER_RODADA, COMEBACK)

- [ ] 3.1 Adicionar método `evaluateRoundEnd(Long userId, int roundNumber)` em `BadgeService` que avalia: LIDER_RODADA se snapshot.position==1; COMEBACK se subiu 5+ posições vs rodada anterior; PALPITOU_TUDO se usuário palpitou em todas as partidas da rodada, verificando com testes unitários que cada critério concede o badge idempotentemente
- [ ] 3.2 Integrar a chamada a `evaluateRoundEnd` no listener do evento de rodada (item 2.2), verificando em teste de integração que o badge correto é concedido ao líder e ao "comebacker"

## 4. Exibição no Perfil

- [ ] 4.1 Incluir `List<UserBadge>` no `ProfileController` (via `BadgeService.getUserBadges`) e renderizar no template `profile/index.html`, verificando visualmente que badges aparecem com emoji, nome e descrição ao acessar `/profile`
- [ ] 4.2 Criar fragmento/bloco de teste de template para confirmar que a lista de badges é renderizada quando há dados, verificando o HTML gerado contém os nomes dos badges

## 5. Gráfico de Evolução no Perfil

- [ ] 5.1 Estender `ProfileStats` e `ProfileController` para incluir `List<RoundSnapshot>` (posição e pontos por rodada apurada) no model, verificando que os dados chegam ao template em teste de controller (MockMvc)
- [ ] 5.2 Criar seção "Evolução" no template `profile/index.html` com gráfico simples (SVG inline ou canvas + JS mínimo, Chart.js via CDN como fallback graceful), verificando visualmente que posição e pontos por rodada são desenhados corretamente com dados de exemplo

## 6. Validação Final

- [ ] 6.1 Executar `./mvnw test` e garantir 100% de sucesso incluindo os novos testes de gamification, snapshot e profile, verificando que não há falhas e o build fica verde
- [ ] 6.2 Executar o app localmente com Docker Compose, criar palpites e simular apuração de rodada para observar badges e gráfico de evolução, verificando manualmente que o fluxo end-to-end funciona sem erros no console
