# Wayfinder Map — Bolão Copa do Mundo Feminina FIFA Brasil 2027™

## Destination

Arquitetura técnica, modelo de dados e especificações de UI consolidados e validados para o Bolão Copa 2027 (Grupo Único), com implementação de monolito modular em Java 25, Spring Boot 4, Thymeleaf + HTMX, PostgreSQL e Docker, pronto para execução e usabilidade espetacular.

## Notes

- **Domínio:** Bolão esportivo gamificado para a Copa do Mundo Feminina da FIFA Brasil 2027™.
- **Escopo Social:** Grupo Único (Single-Tenant / Comunidade fechada).
- **Fuso Oficial:** Horário de Brasília (BRT / UTC-3).
- **Documento Canônico de Requisitos:** [`requisitos-bolao-copa-mundo-feminina-2027.md`](file:///home/rosner/bolao2027/.wayfinder/requisitos-bolao-copa-mundo-feminina-2027.md) (v3.0).
- **Diretrizes de Arquitetura:** DDD, Clean Architecture, Monolito Modular, usabilidade avançada com HTMX + Thymeleaf, Docker Compose local com PostgreSQL.

## Decisions so far

- [001 Base Oficial FIFA 2027](file:///home/rosner/bolao2027/.wayfinder/tickets/001-base-oficial-fifa.md): 32 seleções, 8 grupos, 64 jogos, 24/06 a 25/07/2027, abertura e final no Maracanã.
- [002 Arquitetura de Grupo Único](file:///home/rosner/bolao2027/.wayfinder/tickets/002-arquitetura-grupo-unico.md): Sistema dedicado exclusivamente a um bolão comunitário/corporativo fechado com ranking unificado.
- [003 Regra de Mata-Mata Travada](file:///home/rosner/bolao2027/.wayfinder/tickets/003-regra-mata-mata-120min.md): Placar oficial apurado ao final dos 120 minutos (com prorrogação); havendo pênaltis, registra-se empate e gols de penalidades são desconsiderados.
- [004 Ordem de Desempate Consolidada](file:///home/rosner/bolao2027/.wayfinder/tickets/004-ordem-desempate.md): 1º Placar exato (6 pts) → 2º Acerto parcial (3 pts) → 3º Rodadas anteriores (a partir da R2) → 4º Ordem alfabética.
- [005 Transparência Social Pós-Bloqueio](file:///home/rosner/bolao2027/.wayfinder/tickets/005-bloqueio-transparencia.md): Bloqueio rígido a T - 1h e liberação imediata da visualização pública dos palpites dos concorrentes.
- [006 Recuperação de Acesso](file:///home/rosner/bolao2027/.wayfinder/tickets/006-recuperacao-acesso.md): Adição de recuperação de senha via link seguro por e-mail com token temporário.
- [007 Stack Tecnológica e Infraestrutura](file:///home/rosner/bolao2027/.wayfinder/tickets/007-stack-tecnologica-infraestrutura.md): Java 25, Spring Boot 4.1.x, Thymeleaf + HTMX, PostgreSQL em container Docker, arquitetura modular DDD.
- [011 Motor de Pontuação Oficial](file:///home/rosner/bolao2027/src/main/java/com/bolao2027/modules/scoring/domain/ScoringEngine.java): Implementação de ScoringEngine (6/3/2/1/0 pts, regra 120min de mata-mata, empate em pênaltis) e ScoringService idempotente.

## Frontier (Open Tickets)

- [008 Modelagem DDD e Estrutura Modular](file:///home/rosner/bolao2027/.wayfinder/tickets/008-modelagem-ddd-modulos-ddl.md): Delimitação dos bounded contexts (identity, tournament, prediction, scoring, ranking) e DDL Flyway.
- [009 Design de UI/UX Clean com HTMX](file:///home/rosner/bolao2027/.wayfinder/tickets/009-ux-usabilidade-avancada-htmx-clean.md): Design tokens, microinterações, live updates de palpites, countdown em tempo real e visualização espetacular.
- [010 Docker Compose para Execução Local](file:///home/rosner/bolao2027/.wayfinder/tickets/010-docker-compose-ambiente-local.md): Orquestração de containers para PostgreSQL e App Spring Boot 4 (Java 25).

## Not yet specified

- **Automação de Avanço de Fase no Mata-Mata:** Mecanismo para popular automaticamente seleções classificadas das oitavas às finais com base na pontuação dos grupos.
- **Estratégia de Notificação:** Avisos de proximidade de encerramento do prazo de palpite (push web, e-mail ou webhook/bot de mensagens).
- **Gamificação Avançada:** Sistema de conquistas, badges visuais e ranking histórico de performance rodada a rodada.

## Out of scope

- Multi-tenancy / criação aberta de ligas públicas por qualquer usuário (decisão travada: grupo único).
- Movimentação financeira, apostas em dinheiro real e custódia de valores.
- Transmissão ao vivo de jogos em vídeo.
- Integração por API paga de resultados em tempo real (alimentação manual segura pelo administrador).
