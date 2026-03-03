-- V1: TNS schema and SIM consumption snapshot hypertable
-- Requires TimescaleDB extension (pre-installed in timescale/timescaledb-ha Docker image).

CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE SCHEMA IF NOT EXISTS tns;

CREATE TABLE IF NOT EXISTS tns.tns_sim_consumption_snapshot (
    sim_id                   INTEGER      NOT NULL,
    recorded_at              TIMESTAMPTZ  NOT NULL,
    iccid                    VARCHAR(22),
    msisdn                   VARCHAR(32),
    soldplan_id              INTEGER,
    soldplan_name            VARCHAR(128),
    soldplan_consumption_f   FLOAT8,
    excess_consumption_f     FLOAT8,
    line_total_f             FLOAT8,
    CONSTRAINT pk_tns_sim_consumption_snapshot PRIMARY KEY (sim_id, recorded_at)
);

-- Convert to TimescaleDB hypertable partitioned by recorded_at.
-- chunk_time_interval = 1 day: sized for 150 SIMs at 10-min cadence (~21,600 rows/day).
-- To resize without data loss: SELECT set_chunk_time_interval('tns.tns_sim_consumption_snapshot', INTERVAL 'X days');
-- See: docs/timescaledb-chunk-interval-sizing.md for full sizing rationale and recalculation guide.
SELECT create_hypertable(
    'tns.tns_sim_consumption_snapshot',
    'recorded_at',
    chunk_time_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

-- Secondary index for efficient per-SIM time-range queries
CREATE INDEX IF NOT EXISTS idx_tns_sim_consumption_sim_time
    ON tns.tns_sim_consumption_snapshot (sim_id, recorded_at DESC);

