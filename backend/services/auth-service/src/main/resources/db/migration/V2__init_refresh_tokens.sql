CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID                     NOT NULL,

    token      VARCHAR(2048)            NOT NULL UNIQUE,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    revoked    BOOLEAN                  NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_refresh_tokens_users
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);