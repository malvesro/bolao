# Wayfinder Map — Bolão Copa do Mundo Feminina FIFA Brasil 2027™

## Destination

Arquitetura técnica, modelo de dados e especificações de UI consolidados e validados para o Bolão Copa 2027 (Grupo Único), prontos para a implementação do software sem bloqueios conceituais.

## Notes

- **Domínio:** Bolão esportivo gamificado para a Copa do Mundo Feminina da FIFA Brasil 2027™.
- **Escopo Social:** Grupo Único (Single-Tenant / Comunidade fechada).
- **Fuso Oficial:** Horário de Brasília (BRT / UTC-3).
- **Documento Canônico de Requisitos:** [`requisitos-bolao-copa-mundo-feminina-2027.md`](file:///home/rosner/bolao2027/requisitos-bolao-copa-mundo-feminina-2027.md) (v3.0).

## Decisions so far

- [Base Oficial FIFA 2027](requisitos-bolao-copa-mundo-feminina-2027.md#L18-L25): 32 seleções, 8 grupos, 64 jogos, 24/06 a 25/07/2027, abertura e final no Maracanã.
- [Arquitetura de Grupo Único](requisitos-bolao-copa-mundo-feminina-2027.md#L11-L15): Sistema dedicado exclusivamente a um bolão comunitário/corporativo fechado com ranking unificado.
- [Regra de Mata-Mata Travada](requisitos-bolao-copa-mundo-feminina-2027.md#L41-L50): Placar oficial apurado ao final dos 120 minutos (com prorrogação); havendo pênaltis, registra-se empate e gols de penalidades são desconsiderados.
- [Ordem de Desempate Consolidada](requisitos-bolao-copa-mundo-feminina-2027.md#L73-L80): 1º Placar exato (6 pts) → 2º Acerto parcial (3 pts) → 3º Rodadas anteriores (a partir da R2) → 4º Ordem alfabética.
- [Transparência Social Pós-Bloqueio](requisitos-bolao-copa-mundo-feminina-2027.md#L57-L59): Bloqueio rígido a $T - 1h$ e liberação imediata da visualização pública dos palpites dos concorrentes.
- [Recuperação de Acesso](requisitos-bolao-copa-mundo-feminina-2027.md#L160-L162): Adição de recuperação de senha via link seguro por e-mail, dispensando regras de expiração de senha no torneio de 30 dias.

## Not yet specified

- **Stack Tecnológica e Infraestrutura:** Escolha do framework frontend/backend, banco de dados e serviço de envio de e-mails transacionais (ex.: Next.js App Router + Supabase/PostgreSQL + Resend).
- **Modelagem Relacional de Dados (DDL):** Estrutura detalhada de tabelas (`users`, `matches`, `predictions`, `rounds`, `ranking_cache`, `audit_logs`).
- **Automação de Avanço de Fase no Mata-Mata:** Mecanismo para popular automaticamente seleções classificadas das oitavas às finais com base na pontuação dos grupos.
- **Estratégia de Notificação:** Avisos de proximidade de encerramento do prazo de palpite (push web, e-mail ou webhook/bot de mensagens).

## Out of scope

- Multi-tenancy / criação aberta de ligas públicas por qualquer usuário (decisão travada: grupo único).
- Movimentação financeira, apostas em dinheiro real e custódia de valores.
- Transmissão ao vivo de jogos em vídeo.
- Integração por API paga de resultados em tempo real (alimentação manual segura pelo administrador).
