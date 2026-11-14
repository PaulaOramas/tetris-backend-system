-- V1__init_schema.sql: Initial schema for players, games, and moves

-- Players / Users
CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'ROLE_PLAYER',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);

-- Games
CREATE TABLE IF NOT EXISTS games (
    id            UUID PRIMARY KEY,
    user_id       UUID REFERENCES users (id) ON DELETE SET NULL,
    status        VARCHAR(20) NOT NULL,
    score         INTEGER NOT NULL DEFAULT 0,
    level         INTEGER NOT NULL DEFAULT 1,
    lines_cleared INTEGER NOT NULL DEFAULT 0,
    game_state    JSONB NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_games_user_id ON games (user_id);
CREATE INDEX IF NOT EXISTS idx_games_status ON games (status);
CREATE INDEX IF NOT EXISTS idx_games_score ON games (score DESC);

-- Game Moves (audit trail / event log)
CREATE TABLE IF NOT EXISTS game_moves (
    id          BIGSERIAL PRIMARY KEY,
    game_id     UUID NOT NULL REFERENCES games (id) ON DELETE CASCADE,
    move_type   VARCHAR(20) NOT NULL,
    move_number INTEGER NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_game_move_sequence UNIQUE (game_id, move_number)
);

CREATE INDEX IF NOT EXISTS idx_game_moves_game_id ON game_moves (game_id);
