# Proposal

## Why

O sistema atual concede badges por desempenhos individuais (placar exato, sequência de acertos) mas não fornece ao participante feedback visual da evolução da sua posição e pontos ao longo das rodadas. Além disso, a conquista do badge "Líder da Rodada" já existe no código mas não está conectado ao fluxo de exibição. HU-17 pede um gráfico de evolução rodada a rodada e a experiência atual não entrega isso.

## What Changes

- Implementar gráfico de evolução da posição e pontuação rodada a rodada (HU-17)
- Expandir sistema de badges gamificadas com métricas de desempenho por rodada (HU-18)
  - Conectar badge LIDER_RODADA ao BadgeService existente
  - Possibly add new badge metrics based on progression

## Capabilities

### New Capabilities

- `gamification`: Novo caminho de spec existente. Capabildiade que engloba o painel visual gamificado: gráficos de evolução rodada a rodada e conquista de selos/badges por desempenhos notáveis (ex.: "Mestre do Placar Exato", "Sequência Imbatível", "Líder da Rodada"). O spec já foi criado em `openspec/specs/gamification/spec.md` nesta migração. Este change não adiciona novo spec caminho mas expande requisitos dentro da capability `gamification` já declarada. Nenhuma nova capability path é necessária — a existente será preenchida com os requisitos novos (evolução rodada a rodada + métricas de badge).

### Modified Capabilities

- `gamification`: Requisitos de Evolution Graph e Round Leaderboard agora se aplicam. O spec em `openspec/specs/gamification/spec.md` será atualizado para incluir: (a) Requisito de gráfico de posição/pontuação rodada a rodada e (b) Conquista automática de badge LIDER_RODADA pelo BadgeService existente quando o usuário lidera o ranking ao final de qualquer rodada.

## Impact

- BadgeService já avalia LIDER_RODADA e COMEBACK; change conecta esses resultados ao painel de exibição
- RankingService.buildLeaderboardUpToRound já suporta histórico por round; change pode adicionar endpoint específico para gráfico de evolução por rodada
- Thymeleaf templates de leaderboard/fragments já existentes; novos fragmentos podem ser adicionados para o gráfico