# gamification Specification

## Purpose

Enriquece a experiência do participante com um painel visual gamificado: gráficos de evolução de desempenho rodada a rodada e selos/badges conquistados por desempenhos notáveis, incentivando engajamento ao longo do torneio.

## Requirements

### Requirement: Gráfico de Evolução por Rodada

O sistema DEVE permitir que o participante visualize um gráfico de evolução da sua posição no ranking e da sua pontuação, rodada a rodada (HU-17).

#### Scenario: Consulta da evolução
- **WHEN** o participante acessa seu painel de desempenho
- **THEN** um gráfico exibe posição e pontuação ao longo das rodadas já apuradas

### Requirement: Badges por Desempenho Notável

O sistema DEVE conceder selos/badges gamificados por desempenhos notáveis (ex.: "Mestre do Placar Exato", "Sequência Imbatível") e exibi-los ao participante (HU-18).

#### Scenario: Conquista de badge
- **WHEN** o participante atinge o critério de um badge (ex.: determinada quantidade de placares exatos)
- **THEN** o badge é concedido e passa a ser exibido em seu perfil

#### Scenario: Idempotência da concessão
- **WHEN** o participante já possui um badge e volta a satisfazer seu critério
- **THEN** o badge não é concedido em duplicidade
