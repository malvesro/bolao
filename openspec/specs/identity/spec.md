# identity Specification

## Purpose

Gerencia o ciclo de vida de acesso dos participantes do bolão: registro, autenticação segura, troca voluntária de senha e recuperação de senha esquecida via e-mail. Opera no modelo de Grupo Único (instância dedicada a uma organização ou comunidade fechada), sem multi-tenancy.

## Requirements

### Requirement: Registro de Participante

O sistema DEVE permitir que um novo participante se registre na plataforma, passando a concorrer no ranking unificado.

#### Scenario: Registro bem-sucedido
- **WHEN** um visitante envia o formulário de registro com dados válidos
- **THEN** o sistema cria a conta e permite o acesso às áreas autenticadas

### Requirement: Autenticação Segura

O sistema DEVE exigir autenticação para as áreas internas do bolão e restringir funcionalidades administrativas ao perfil de administrador.

#### Scenario: Acesso a área protegida sem autenticação
- **WHEN** um usuário não autenticado acessa uma rota protegida
- **THEN** o sistema o redireciona para a tela de login

#### Scenario: Acesso a área administrativa por participante comum
- **WHEN** um participante sem perfil de administrador acessa uma rota administrativa
- **THEN** o sistema nega o acesso

### Requirement: Troca de Senha Voluntária

O sistema DEVE permitir que o participante autenticado altere sua senha a qualquer momento na tela "Trocar Senha" (HU-12).

#### Scenario: Troca de senha bem-sucedida
- **WHEN** o participante autenticado informa a senha atual e a nova senha válida
- **THEN** o sistema atualiza a credencial e confirma a alteração em tela

#### Scenario: Senha atual incorreta
- **WHEN** o participante informa a senha atual incorreta
- **THEN** o sistema rejeita a alteração sem trocar a senha

### Requirement: Orientação de Senha Segura

O sistema DEVE orientar os participantes sobre boas práticas de senha segura, sem impor bloqueios desnecessários durante os 30 dias do campeonato (HU-13).

#### Scenario: Exibição de orientação
- **WHEN** o participante acessa telas de criação ou troca de senha
- **THEN** o sistema exibe orientações de senha segura sem impedir senhas válidas que atendam ao mínimo exigido

### Requirement: Recuperação de Senha por E-mail

O sistema DEVE oferecer recuperação de senha via link seguro enviado ao e-mail cadastrado (HU-21), com token único de expiração entre 30 e 60 minutos, e NÃO DEVE revelar se um e-mail existe no banco (proteção contra enumeração de usuários).

#### Scenario: Solicitação de recuperação
- **WHEN** o participante envia o formulário com um endereço de e-mail
- **THEN** o sistema exibe a mesma mensagem genérica de confirmação, independentemente de o e-mail estar ou não cadastrado

#### Scenario: E-mail cadastrado
- **WHEN** o e-mail informado pertence a uma conta existente
- **THEN** o sistema gera um token único de redefinição, com expiração de 30 a 60 minutos, e envia o link de redefinição ao e-mail

#### Scenario: Redefinição com token válido
- **WHEN** o participante acessa o link com token válido e informa a nova senha com confirmação
- **THEN** o sistema redefine a senha e invalida o token

#### Scenario: Token expirado ou inválido
- **WHEN** o participante acessa um link de redefinição com token expirado ou inexistente
- **THEN** o sistema recusa a redefinição e orienta uma nova solicitação
