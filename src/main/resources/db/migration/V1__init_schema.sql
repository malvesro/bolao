-- =============================================================
-- V1 — Schema inicial do Bolão Copa do Mundo Feminina 2027
-- =============================================================

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS matches (
    id              BIGSERIAL PRIMARY KEY,
    match_number    INTEGER      NOT NULL UNIQUE,
    round_number    INTEGER      NOT NULL,
    stage           VARCHAR(30)  NOT NULL,
    group_letter    VARCHAR(5),
    home_team       VARCHAR(60)  NOT NULL,
    away_team       VARCHAR(60)  NOT NULL,
    match_date_time TIMESTAMP    NOT NULL,
    stadium         VARCHAR(80)  NOT NULL,
    city            VARCHAR(60)  NOT NULL,
    home_score120   INTEGER,
    away_score120   INTEGER,
    penalty_winner  VARCHAR(60),
    status          VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED'
);

CREATE TABLE IF NOT EXISTS predictions (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    match_id      BIGINT   NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    home_score    INTEGER  NOT NULL,
    away_score    INTEGER  NOT NULL,
    points_awarded INTEGER,
    score_reason  VARCHAR(100),
    submitted_at  TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_match UNIQUE (user_id, match_id)
);

CREATE INDEX IF NOT EXISTS idx_predictions_user_id  ON predictions(user_id);
CREATE INDEX IF NOT EXISTS idx_predictions_match_id ON predictions(match_id);
CREATE INDEX IF NOT EXISTS idx_matches_round        ON matches(round_number);
CREATE INDEX IF NOT EXISTS idx_matches_stage        ON matches(stage);
CREATE INDEX IF NOT EXISTS idx_matches_status       ON matches(status);
