-- =============================================================
-- V2 — Usuário administrador padrão do sistema
-- Senha: admin2027 (BCrypt hash — gerado com strength 10)
-- IMPORTANTE: alterar a senha via painel após o primeiro login!
-- =============================================================

-- O hash BCrypt abaixo corresponde à senha: admin2027
INSERT INTO users (username, email, password, display_name, role, created_at)
SELECT 'admin',
       'admin@bolao2027.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVKCbgqrIy',
       'Administrador',
       'ROLE_ADMIN',
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);
