---
id: "004"
title: "Ordem Consolidada de Desempate"
type: "wayfinder:grilling"
status: "closed"
---

## Question

Quais os critérios exatos para desempatar participantes com a mesma pontuação acumulada?

## Resolution

A ordem matemática mandatória é:
1. Maior quantidade de acertos de placar exato (6 pontos).
2. Maior quantidade de acertos de vencedor/empate com gols de uma equipe (3 pontos).
3. Maior pontuação acumulada em rodadas anteriores (a partir da Rodada 2; ignorado na Rodada 1).
4. Ordem alfabética pelo nome de exibição normalizado (minúsculas, sem acentos).
Recalculado de forma idempotente a cada partida finalizada.
