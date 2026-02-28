-- V1: Base schema
-- Creates the integration_event table used by all integration modules.

CREATE TABLE IF NOT EXISTS integration_event (
    id             UUID        NOT NULL DEFAULT gen_random_uuid(),
    integration_name VARCHAR(64)  NOT NULL,
    event_type     VARCHAR(128) NOT NULL,
    payload        TEXT,
    occurred_at    TIMESTAMPTZ  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT pk_integration_event PRIMARY KEY (id, occurred_at)
);

-- Index for fast lookup by integration + time range
CREATE INDEX IF NOT EXISTS idx_integration_event_name_occurred
    ON integration_event (integration_name, occurred_at DESC);

