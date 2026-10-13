# ranking Specification

## Purpose

Mantém o ranking geral unificado dos participantes com pontuação atualizada em tempo real, aplicando automaticamente os 4 critérios de desempate consolidados e permitindo auditoria interativa do critério que desfez cada empate.

## Requirements

### Requirement: Ranking Geral Unificado

O sistema DEVE apresentar o ranking geral unificado com a pontuação total de todos os participantes, atualizado a cada partida apurada (HU-09).

#### Scenario: Atualização após apuração
- **WHEN** o resultado oficial de uma partida é apurado
- **THEN** o ranking reflete a nova pontuação total de todos os participantes

### Requirement: Critérios de Desempate

O sistema DEVE desempatar participantes com pontuação total igual aplicando rigorosamente a ordem (regra 1.3.5, HU-10):

1. Maior quantidade de acertos de placar exato (acertos de 6 pontos)
2. Maior quantidade de acertos de vencedor/empate com gols de uma equipe (acertos de 3 pontos)
3. Maior pontuação acumulada em rodadas anteriores (ignorado na Rodada 1, pois é 0 para todos)
4. Ordem alfabética pelo nome de exibição normalizado (sem acentos, minúsculo, sem distinção de maiúsculas/minúsculas)

Os critérios DEVEM ser recalculados a cada partida apurada.

#### Scenario: Desempate por placares exatos
- **WHEN** dois participantes têm a mesma pontuação total
- **THEN** quem acumulou mais acertos de placar exato (6 pts) fica à frente

#### Scenario: Persistência do empate até o nome
- **WHEN** dois participantes empatam em pontos totais, acertos de 6 pts, acertos de 3 pts e pontuação de rodadas anteriores
- **THEN** a ordem alfabética do nome normalizado resolve a classificação

#### Scenario: Rodada 1 sem histórico
- **WHEN** a classificação é calculada durante a Rodada 1
- **THEN** o critério de pontuação em rodadas anteriores é ignorado

### Requirement: Detalhamento do Desempate

O sistema DEVE permitir que o participante visualize o detalhamento de qual critério desfez o empate entre dois ou mais competidores (HU-11).

#### Scenario: Auditoria de empate
- **WHEN** um participante consulta o detalhamento de um empate no ranking
- **THEN** o sistema indica qual critério (1º, 2º, 3º ou 4º) determinou a classificação
