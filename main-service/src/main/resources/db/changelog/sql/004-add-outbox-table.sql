--liquibase formatted sql

--changeset ilmira:004-1
CREATE TABLE outbox_messages (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic         VARCHAR(255) NOT NULL,
    message_key   VARCHAR(255),
    payload_type  VARCHAR(255) NOT NULL,
    payload       JSONB        NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at  TIMESTAMP
);

--changeset ilmira:004-2
CREATE INDEX idx_outbox_unpublished
    ON outbox_messages (created_at)
    WHERE published_at IS NULL;