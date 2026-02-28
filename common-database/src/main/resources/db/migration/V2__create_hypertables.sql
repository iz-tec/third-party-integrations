-- V2: TimescaleDB hypertables
-- Requires TimescaleDB extension to be installed in PostgreSQL.
-- The extension is pre-installed in the timescale/timescaledb-ha Docker image.

CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Convert integration_event into a hypertable partitioned by occurred_at.
-- chunk_time_interval = 7 days means each chunk covers one week of data.
SELECT create_hypertable(
    'integration_event',
    'occurred_at',
    chunk_time_interval => INTERVAL '7 days',
    if_not_exists => TRUE
);

