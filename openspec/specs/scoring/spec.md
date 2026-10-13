# scoring Specification

## Purpose

Calcula automaticamente a pontuação dos palpites quando o resultado oficial é lançado, aplicando a tabela decrescente e excludente 6/3/2/1/0 e a regra oficial de 120 minutos no mata-mata (empate oficial em caso de pênaltis). Garante recálculo idempotente em correções e atribui a cada palpite seu motivo explícito de pontuação.

## Requirements

### Requirement: Tabela de Pontuação 6/3/2/1/0

O sistema DEVE atribuir pontuação decrescente e excludente — apenas a maior pontuação atingida no jogo é atribuída (regra 1.3.1, HU-07):

- **6 pontos**: acerto do placar exato (gols de ambas as equipes iguais ao resultado oficial)
- **3 pontos**: acerto do vencedor/empate + gols exatos de 1 equipe
- **2 pontos**: acerto apenas do resultado (vitória, derrota ou empate), errando os gols das duas equipes
- **1 ponto**: acerto apenas dos gols de 1 equipe, errando o resultado geral
- **0 pontos**: demais combinações

#### Scenario: Hierarquia estrita
- **WHEN** um palpite satisfaz mais de uma condição da tabela
- **THEN** apenas a maior pontuação atingida é atribuída

#### Scenario: Placar exato
- **WHEN** o palpite é 2x1 e o resultado oficial é 2x1
- **THEN** o participante recebe 6 pontos

#### Scenario: Resultado mais gols de uma equipe
- **WHEN** o palpite é 2x1, o resultado oficial é 3x1 (vitória do mesmo time)
- **THEN** o participante recebe 3 pontos

#### Scenario: Apenas resultado
- **WHEN** o palpite é 2x0 e o resultado oficial é 1x0 (outro placar, mesmo vencedor)
- **THEN** o participante recebe 2 pontos

#### Scenario: Apenas gols de uma equipe
- **WHEN** o palpite é 1x0 e o resultado oficial é 2x3
- **THEN** o participante recebe 1 ponto (acertou os gols da equipe A, errou o resultado)

### Requirement: Regra de 120 Minutos no Mata-Mata

O sistema DEVE considerar como resultado oficial das partidas eliminatórias o placar ao final dos 120 minutos de bola rolando (90 minutos + prorrogação). Em decisão por pênaltis, o resultado oficial é EMPATE pelo placar dos 120 minutos e os gols da disputa de pênaltis NÃO são somados ao placar (regra 1.3.2).

#### Scenario: Exemplo oficial com pênaltis
- **WHEN** a partida termina 1x1 nos 90 minutos, 2x2 na prorrogação e é decidida nos pênaltis (5x4)
- **THEN** o placar oficial do bolão é 2x2: quem palpitou 2x2 recebe 6 pontos; quem palpitou 1x1 ou 0x0 recebe 2 pontos (acertou empate); quem palpitou 2x1 para qualquer time recebe 1 ponto (acertou gols de um time); quem palpitou vitória do vencedor dos pênaltis (ex.: 3x2) recebe 0 pontos

### Requirement: Cálculo Automático ao Apurar

O sistema DEVE calcular automaticamente a pontuação de todos os palpites de uma partida assim que o administrador lançar o resultado oficial (HU-07).

#### Scenario: Apuração de partida
- **WHEN** o resultado oficial de uma partida é lançado
- **THEN** todos os palpites daquela partida recebem pontuação conforme a tabela, sem intervenção manual

### Requirement: Recálculo Idempotente

O sistema DEVE suportar correção do resultado oficial com recálculo automático e idempotente das pontuações, sem duplicidade de pontos (regra 1.3.3, HU-08).

#### Scenario: Correção de resultado
- **WHEN** o administrador corrige o placar de uma partida já apurada
- **THEN** as pontuações são recalculadas substituindo os valores anteriores, sem acumular nem duplicar pontos

### Requirement: Motivo Explícito da Pontuação

O sistema DEVE exibir ao participante, para cada partida encerrada, quantos pontos obteve acompanhados do motivo explícito (ex.: "Placar exato", "Resultado + gols time A") (HU-06).

#### Scenario: Exibição de pontos com justificativa
- **WHEN** o participante consulta uma partida apurada
- **THEN** os pontos obtidos são exibidos junto à justificativa correspondente à regra aplicada
