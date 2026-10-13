---
id: "010"
title: "Docker Compose e Ambiente de Execução Local"
type: "wayfinder:task"
status: "open"
blocked-by: ["008"]
---

## Question

Como configurar o `docker-compose.yml` e o `Dockerfile` com Java 25 e PostgreSQL para que a aplicação e o banco subam em containers de forma reprodutível e rápida?

## Details

1. Serviço `postgres` configurado com volume persistente, healthcheck e credenciais de desenvolvimento.
2. Serviço `app` construído com suporte a Java 25 e Spring Boot 4.
3. Perfis Spring (ex: `local`, `docker`) ajustando URL do JDBC.
