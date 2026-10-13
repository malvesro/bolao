---
id: "008"
title: "Modelagem DDD, Módulos e Esquema Relacional (DDL)"
type: "wayfinder:task"
status: "open"
blocks: ["010"]
---

## Question

Como estruturar os bounded contexts do monolito modular (identity, tournament, prediction, scoring, ranking) com regras DDD ricas e schema de banco PostgreSQL consistente?

## Details

1. Módulo `identity`: User, Role, PasswordResetToken.
2. Módulo `tournament`: Match, Stage, Round, Team, Stadium.
3. Módulo `prediction`: Prediction (palpite), validação de corte T-1h, visualização agregada.
4. Módulo `scoring`: CalculationEngine (6/3/2/1/0, regra 120min + pênaltis).
5. Módulo `ranking`: RankingEntry, histórico por rodada e ordenação com os 4 critérios de desempate.
