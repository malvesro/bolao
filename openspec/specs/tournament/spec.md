# tournament Specification

## Purpose

Mantém a base oficial do torneio (calendário, seleções, fases e rodadas da Copa do Mundo Feminina FIFA Brasil 2027™), o chaveamento atualizado conforme as seleções avançam e o lançamento/revisão dos resultados oficiais pelo administrador. Todos os horários são padronizados no fuso de Brasília (BRT / UTC-3).

## Requirements

### Requirement: Base Oficial do Torneio

O sistema DEVE manter pré-carregada a tabela oficial da Copa do Mundo Feminina FIFA Brasil 2027™: 32 seleções em 8 grupos (A–H) e 64 partidas (48 na fase de grupos + 16 no mata-mata), disputadas entre 24/06/2027 e 25/07/2027.

#### Scenario: Inicialização da base
- **WHEN** a aplicação é iniciada pela primeira vez
- **THEN** as 64 partidas oficiais estão disponíveis para consulta, organizadas por rodada e fase

### Requirement: Estrutura de Rodadas

O sistema DEVE dividir o torneio em 7 rodadas oficiais para efeito de histórico e desempate: 3 rodadas de fase de grupos (16 partidas cada), oitavas (8), quartas (4), semifinais (2) e disputa de 3º lugar + final (2).

#### Scenario: Consulta por rodada
- **WHEN** um usuário solicita as partidas de uma rodada específica
- **THEN** o sistema retorna exatamente as partidas daquela rodada conforme a divisão oficial

### Requirement: Fuso Horário de Brasília

O sistema DEVE exibir todas as datas e horários de partidas padronizados no horário de Brasília (BRT / UTC-3), independentemente do fuso do dispositivo do usuário.

#### Scenario: Exibição de horário de partida
- **WHEN** qualquer partida é exibida na interface
- **THEN** o horário mostrado é o oficial em BRT

### Requirement: Chaveamento e Tabela de Grupos

O sistema DEVE permitir que o participante visualize a tabela completa dos grupos e o chaveamento oficial das fases finais, atualizado conforme as seleções avançam no mata-mata (HU-19).

#### Scenario: Chaveamento após classificação
- **WHEN** o administrador confirma as seleções classificadas de uma fase
- **THEN** o chaveamento da fase seguinte reflete os confrontos atualizados

### Requirement: Avanço de Confrontos no Mata-Mata

O sistema DEVE permitir que o administrador confirme as seleções classificadas, promovendo os vencedores aos confrontos da fase seguinte.

#### Scenario: Avanço da fase de grupos para as oitavas
- **WHEN** o administrador aciona o avanço de grupos após a conclusão da fase de grupos
- **THEN** os confrontos das oitavas de final são preenchidos com as seleções classificadas

#### Scenario: Avanço entre fases eliminatórias
- **WHEN** o administrador confirma os vencedores de uma fase eliminatória
- **THEN** a fase seguinte recebe os vencedores nos confrontos correspondentes

### Requirement: Lançamento e Correção de Resultados Oficiais

O sistema DEVE permitir que o administrador lance ou corrija o resultado oficial de uma partida, disparando recálculo automático e idempotente das pontuações, com log de auditoria (HU-08).

#### Scenario: Lançamento de resultado
- **WHEN** o administrador registra o placar oficial de uma partida
- **THEN** a partida é marcada como apurada e a pontuação dos palpites é calculada automaticamente

#### Scenario: Correção de resultado
- **WHEN** o administrador corrige um placar lançado por engano
- **THEN** as pontuações são recalculadas de forma idempotente, sem duplicidade de pontos, e a correção fica registrada em auditoria

#### Scenario: Placar de mata-mata com pênaltis
- **WHEN** o administrador lança o resultado de uma partida eliminatória decidida nos pênaltis
- **THEN** o placar registrado reflete o resultado ao fim dos 120 minutos (empate), sem somar os gols da disputa de pênaltis
