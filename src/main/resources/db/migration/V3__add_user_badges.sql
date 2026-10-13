-- V3 — Tabela de badges conquistados pelos participantes
CREATE TABLE IF NOT EXISTS user_badges (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_type VARCHAR(50) NOT NULL,
    earned_at  TIMESTAMP   NOT NULL,
    CONSTRAINT uk_user_badge_type UNIQUE (user_id, badge_type)
);

CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON user_badges(user_id);
