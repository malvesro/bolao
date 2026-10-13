---
id: "007"
title: "Stack Tecnológica e Infraestrutura do Monolito"
type: "wayfinder:grilling"
status: "closed"
---

## Question

Qual a stack tecnológica, modelo de empacotamento e arquitetura base do sistema?

## Resolution

- **Linguagem & Runtime:** Java 25.
- **Framework Principal:** Spring Boot 4.x (versão mais recente, 4.1.x) com Spring Security, Spring Data JPA e Bean Validation.
- **Frontend / UI:** Thymeleaf com HTMX para interatividade reativa sem a complexidade de SPA separado. Estilização moderna, limpa, responsiva (mobile-first) e usabilidade espetacular.
- **Banco de Dados:** PostgreSQL rodando em container Docker.
- **Infraestrutura Local:** Docker Compose orquestrando o banco PostgreSQL e o container da aplicação.
- **Padrão de Arquitetura:** Clean Architecture + Domain-Driven Design (DDD) estruturado como Monolito Modular (bounded contexts desacoplados em módulos: `identity`, `tournament`, `prediction`, `scoring`, `ranking`).
