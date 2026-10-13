# Spec Delta

## MODIFIED Requirements

### Requirement: Gráfico de Evolução por Rodada
O sistema DEVE permitir que o participante visualize um gráfico de evolução da sua posição no ranking e da sua pontuação, rodada a rodada (HU-17).

#### Scenario: Consulta da evolução
- **WHEN** o participante acessa seu painel de desempenho
- **THEN** um gráfico exibe posição e pontuação ao longo das rodadas já apuradas

#### Scenario: Evolução baseada em histórico
- **WHEN** o ranking é recalculado a cada partida apurada
- **THEN** o painel de evolução reflete a posição e pontos do participante como estavam ao final de cada rodada, não apenas no momento atual

#### Scenario: Rodadas ainda não apuradas
- **WHEN** o participante acessa o painel antes de uma rodada ser totalmente apurada
- **THEN** somente as rodadas já concluídas são exibidas no gráfico

### Requirement: Badges por Desempenho Notável
O sistema DEVE conceder selos/badges gamificados por desempenhos notáveis (ex.: "Mestre do Placar Exato", "Sequência Imbatível") e exibi-los ao participante (HU-18).

#### Scenario: Conquista de badge
- **WHEN** o participante atinge o critério de um badge (ex.: determinada quantidade de placares exatos)
- **THEN** o badge é concedido e passa a ser exibido em seu perfil

#### Scenario: Idempotência da concessão
- **WHEN** o participante já possui um badge e volta a satisfazer seu critério
- **THEN** o badge não é concedido em duplicidade

#### Scenario: Badge Líder da Rodada
- **WHEN** uma rodada é totalmente apurada (todas as partidas da rodada com resultado oficial lançado)
- **THEN** o participante que ocupar a 1ª posição no ranking ao final daquela rodada recebe o badge de líder da rodada

#### Scenario: Badge Virada
- **WHEN** ao final de uma rodada o participante subiu 5 ou mais posições no ranking em relação ao final da rodada anterior
- **THEN** o sistema concede o badge de virada

#### Scenario: Badges por marcos de placar exato
- **WHEN** o participante alcança um marco de acertos de placar exato (1, 5 ou 10 ou mais acertos de 6 pontos)
- **THEN** o badge correspondente ao marco é concedido

#### Scenario: Badge rodada completa
- **WHEN** o participante registrou palpite em todas as partidas de uma rodada
- **THEN** o sistema concede o badge de participação completa da rodada
