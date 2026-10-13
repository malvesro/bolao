# prediction Specification

## Purpose

Gerencia os palpites de placar dos participantes: registro e edição dentro da janela permitida, contagem regressiva em BRT, bloqueio automático 1 hora antes de cada partida e transparência pública dos palpites após o corte. Garante a integridade da competição (ninguém altera após a trava, ninguém vê palpites alheios antes dela).

## Requirements

### Requirement: Registro e Edição de Palpite

O sistema DEVE permitir que o participante registre e altere seu palpite de placar (gols da equipe A x gols da equipe B) para qualquer jogo com janela aberta, aceitando apenas inteiros não negativos e confirmando o salvamento com feedback visual imediato (HU-01).

#### Scenario: Registro de palpite válido
- **WHEN** o participante envia um placar com valores inteiros maiores ou iguais a zero dentro da janela
- **THEN** o palpite é salvo e uma confirmação visual é exibida em tela

#### Scenario: Rejeição de valores inválidos
- **WHEN** o participante envia valores negativos, decimais ou caracteres especiais
- **THEN** o sistema rejeita o palpite e informa o motivo

#### Scenario: Edição ilimitada dentro da janela
- **WHEN** faltam mais de 60 minutos para o início da partida (BRT)
- **THEN** o participante pode alterar seu palpite quantas vezes quiser

### Requirement: Contagem Regressiva e Bloqueio Estrito

O sistema DEVE exibir contagem regressiva em tempo real até o corte (início − 1 hora, BRT), com alerta visual diferenciado nos últimos 15 minutos, e DEVE bloquear qualquer gravação ou edição a partir do corte (HU-02, HU-03).

#### Scenario: Bloqueio no backend
- **WHEN** uma requisição de gravação ou edição chega ao backend após o corte (T − 1h)
- **THEN** o sistema rejeita com `403 Forbidden` e mensagem amigável ("Prazo encerrado")

#### Scenario: Transmutação da interface
- **WHEN** a janela se encerra
- **THEN** os campos de edição da partida se transformam em visualização estática

### Requirement: Horários em Horário de Brasília

O sistema DEVE exibir todos os horários de jogos convertidos fixamente para o horário de Brasília (BRT), independente do dispositivo do participante (HU-04).

#### Scenario: Participante em outro fuso
- **WHEN** um participante acessa a lista de partidas de um dispositivo em fuso diferente
- **THEN** todos os horários exibidos permanecem em BRT

### Requirement: Onboarding de Regras

O sistema DEVE apresentar uma explicação visual e resumida das regras de pontuação, incluindo a regra de 120 minutos do mata-mata, no primeiro acesso do participante (HU-05).

#### Scenario: Primeiro acesso
- **WHEN** um novo participante acessa a plataforma pela primeira vez
- **THEN** o resumo das regras (6/3/2/1/0 e regra de 120 min) é exibido de forma acessível

### Requirement: Transparência Pós-Corte de Palpites

O sistema DEVE tornar públicos todos os palpites registrados para uma partida imediatamente após o corte (T − 1h), e NÃO DEVE permitir a nenhum participante comum acesso a palpites alheios antes do corte, com proteção no backend (HU-20).

#### Scenario: Grade pública após o corte
- **WHEN** a janela de palpites de uma partida se encerra
- **THEN** a área de palpites da partida exibe a lista com o nome de cada participante e o placar palpitado

#### Scenario: Sigilo antes do corte
- **WHEN** um participante comum tenta acessar palpites de outros participantes antes do corte
- **THEN** o sistema nega o acesso no backend, expondo apenas o próprio palpite
