## 📧 Mailhog (Captura de E-mails em Dev)

Com `docker compose up`, o Mailhog está disponível em:
- **Web UI**: http://localhost:8025
- **SMTP**: localhost:1025

Todos os e-mails enviados pela aplicação (recuperação de senha, etc.)
são capturados pelo Mailhog e visíveis na interface web.

---

# ⏸️ Como Retomar o Trabalho no Bolão 2027

Este documento permite que você ou qualquer agente de IA retome o trabalho deste projeto a qualquer momento sem perder o contexto nem o progresso.

---

## 📌 Status Atual do Projeto
- **Compilação:** ✅ Sucesso com Java 25 e Spring Boot 4.1.1 (`./mvnw test-compile`).
- **Arquitetura:** Monolito Modular DDD + Clean Architecture.
- **Estrutura Wayfinder:** Totalmente criada em [`.wayfinder/`](.wayfinder/) com requisitos, tickets e checkpoint.
- **Domínio Entregue:** 
  - Módulo `tournament` com 64 jogos oficiais FIFA 2027 pré-carregados.
  - Módulo `identity` com segurança, usuários e tokens de recuperação de senha.
  - Módulo `scoring` com `ScoringEngine` (6/3/2/1/0 pts, regra oficial de 120 minutos e empate em pênaltis) e `ScoringService` idempotente.
  - Entidade `Prediction` com pontuação atribuída e justificativa.

---

## 🚀 Como Continuar em Nova Sessão

Para continuar, basta enviar a mensagem abaixo no chat:

```text
Continuar a implementação do monolito Bolão Copa do Mundo Feminina 2027 a partir do checkpoint em .wayfinder/CHECKPOINT.md.
Stack: Java 25, Spring Boot 4.1.1, Thymeleaf, HTMX, PostgreSQL em Docker Compose.
Executar os próximos passos:
1. Módulo ranking (RankingEntry, RankingTieBreakerComparator com os 4 critérios de desempate e RankingService).
2. Dockerfile Java 25 e docker-compose.yml com PostgreSQL 17.
3. PredictionService + Controllers + Templates Thymeleaf/HTMX com visual clean e alta usabilidade.
4. Testes de Scoring e Ranking com ./mvnw test.
```

---

## 📂 Arquivos de Referência
- **Checkpoint detalhado da sessão:** [`.wayfinder/CHECKPOINT.md`](.wayfinder/CHECKPOINT.md)
- **Mapa canônico de decisões e fronteira:** [`.wayfinder/map.md`](.wayfinder/map.md)
- **Requisitos oficiais do bolão:** [`.wayfinder/requisitos-bolao-copa-mundo-feminina-2027.md`](.wayfinder/requisitos-bolao-copa-mundo-feminina-2027.md)
- **Painel de progresso do Gauntlet:** [`GAUNTLET_PROGRESS.md`](.wayfinder/GAUNTLET_PROGRESS.md)

---

## 🛠️ Comandos de Verificação Rápida
```bash
# Verificar compilação
./mvnw test-compile

# Executar testes
./mvnw test
```
