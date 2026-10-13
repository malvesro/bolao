# support Specification

## Purpose

Oferece um canal integrado de suporte para que participantes reportem dúvidas ou inconsistências com protocolo de acompanhamento, e um painel para o administrador visualizar, filtrar e responder os chamados — reforçando conduta e transparência no bolão.

## Requirements

### Requirement: Canal de Suporte com Protocolo

O sistema DEVE oferecer um canal integrado para o participante reportar dúvidas ou inconsistências ao administrador, gerando um protocolo de acompanhamento para cada chamado (HU-14).

#### Scenario: Abertura de chamado
- **WHEN** o participante envia uma dúvida ou reporte pelo canal de suporte
- **THEN** o sistema registra o chamado e gera um protocolo único de acompanhamento

#### Scenario: Acompanhamento pelo participante
- **WHEN** o participante consulta seus chamados
- **THEN** o sistema exibe o protocolo, o estado e as respostas de cada chamado

### Requirement: Aviso de Resultados Definitivos

O sistema DEVE exibir aviso claro de que os resultados lançados pela administração são definitivos após o encerramento da apuração (HU-15).

#### Scenario: Exibição do aviso
- **WHEN** o participante visualiza partidas apuradas ou o ranking consolidado
- **THEN** o aviso sobre a definitividade dos resultados é apresentado de forma clara

### Requirement: Painel Administrativo de Chamados

O sistema DEVE permitir que o administrador visualize, filtre e responda os chamados abertos pelos participantes (HU-16).

#### Scenario: Filtragem de chamados
- **WHEN** o administrador acessa o painel de chamados
- **THEN** ele pode visualizar e filtrar os chamados por estado ou participante

#### Scenario: Resposta a chamado
- **WHEN** o administrador responde um chamado
- **THEN** a resposta fica disponível ao participante autor, vinculada ao protocolo
