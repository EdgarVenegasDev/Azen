CREATE TABLE users
(
    id         UUID PRIMARY KEY,

    first_name VARCHAR(255)             NOT NULL,
    last_name  VARCHAR(255)             NOT NULL,

    email      VARCHAR(255)             NOT NULL UNIQUE,
    password   VARCHAR(255)             NOT NULL,

    status     VARCHAR(50)              NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    version    INTEGER
);