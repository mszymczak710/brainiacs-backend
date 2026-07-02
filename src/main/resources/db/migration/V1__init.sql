CREATE TABLE users (
    id                  BIGSERIAL PRIMARY KEY,
    first_name          VARCHAR(255) NOT NULL,
    last_name           VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL,
    avatar              BYTEA,
    avatar_content_type VARCHAR(255),
    CONSTRAINT uk_users_email UNIQUE (email)
);