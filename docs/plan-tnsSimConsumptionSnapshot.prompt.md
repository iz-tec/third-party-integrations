# Plan: TNS SIM Consumption Snapshot Hypertable

Store a snapshot of every SIM's consumption fields every 10 minutes using a TimescaleDB
hypertable. The entity and all related infrastructure live exclusively in `integration-tns`.
The `common-database` migration path is dormant (migration files removed, module kept for
future use of `BaseEntity` and `DatabaseAutoConfiguration`).

---

## Decisions made

| Topic | Decision |
|---|---|
| PK strategy | Composite `(sim_id INTEGER, recorded_at TIMESTAMPTZ)` — natural unique key per batch tick |
| Scheduler trigger | `@Scheduled(fixedRate = 600_000)` — fires every 10 min regardless of previous run duration |
| Duplicate handling | Native upsert `ON CONFLICT (sim_id, recorded_at) DO UPDATE SET ...` — overwrites consumption columns with latest values |
| DB schema | Dedicated `tns` PostgreSQL schema — isolates TNS tables from future shared infrastructure |
| Entity location | `integration-tns` module — entity is TNS-specific, not shared |
| `common-database` migrations | Removed `V1__create_integration_event.sql` and `V2__create_hypertables.sql` — folder left empty, dormant for future use |
| Hypertable chunk interval | `INTERVAL '1 day'` — suits 10-min ingestion cadence and future time-bucket aggregations |
| TimescaleDB time-bucket | `time_bucket('1 hour'/'1 day'/'7 days', recorded_at)` with `MAX - MIN` delta on cumulative `_f` fields |

---

## Source fields from `TnsSimResponse`

Fields to snapshot (all others ignored for now):

| `TnsSimResponse` field | DB column | Type | Notes |
|---|---|---|---|
| `id` | `sim_id` | INTEGER | Part of composite PK |
| `iccid` | `iccid` | VARCHAR(22) | Human-readable SIM identifier |
| `msisdn` | `msisdn` | VARCHAR(32) | Phone number assigned to SIM |
| `soldplanId` | `soldplan_id` | INTEGER | Plan identifier |
| `soldplanName` | `soldplan_name` | VARCHAR(128) | Plan name |
| `soldplanConsumptionF` | `soldplan_consumption_f` | FLOAT8 | Cumulative plan consumption in bytes |
| `excessConsumptionF` | `excess_consumption_f` | FLOAT8 | Cumulative excess consumption in bytes |
| `lineTotalF` | `line_total_f` | FLOAT8 | Cumulative total line consumption in bytes |
| _(scheduler tick)_ | `recorded_at` | TIMESTAMPTZ | Part of composite PK — UTC, same value for all SIMs in a batch |

---

## Steps

### Step 1 — Remove dormant common-database migrations

Delete both files from `common-database/src/main/resources/db/migration/`:
- `V1__create_integration_event.sql`
- `V2__create_hypertables.sql`

Leave the folder in place. The module itself (`BaseEntity`, `DatabaseAutoConfiguration`) is
kept for future use.

---

### Step 2 — Delete `IntegrationEvent` and `IntegrationEventRepository`

Remove from `common-database`:
- `src/main/java/io/iztec/tp/commons/database/entity/IntegrationEvent.java`
- `src/main/java/io/iztec/tp/commons/database/repository/IntegrationEventRepository.java`

---

### Step 3 — Clean up `TnsController`

File: `integration-tns/src/main/java/io/iztec/tp/integration/tns/TnsController.java`

Remove all references to `IntegrationEvent` and `IntegrationEventRepository`.
Remove or stub the notification endpoints (stub as `501 Not Implemented` to preserve route
visibility, or remove entirely).

---

### Step 4 — Flyway migration `V1__create_tns_schema_and_snapshot.sql`

Location: `integration-tns/src/main/resources/db/migration/`

```sql
-- V1: TNS schema and SIM consumption snapshot hypertable

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

SELECT create_hypertable(
    'tns.tns_sim_consumption_snapshot',
    'recorded_at',
    chunk_time_interval => INTERVAL '1 day',
    if_not_exists => TRUE
);

CREATE INDEX IF NOT EXISTS idx_tns_sim_consumption_sim_time
    ON tns.tns_sim_consumption_snapshot (sim_id, recorded_at DESC);
```

---

### Step 5 — Update `application.yml`

File: `integration-tns/src/main/resources/application.yml`

Set Flyway to scan only the TNS migration folder (already the default location, just confirm
no reference to `classpath:db/migration` from `common-database` is active):

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    default-schema: tns
```

---

### Step 6 — `@Embeddable` composite PK class `TnsSimConsumptionSnapshotId`

Location: `integration-tns/src/main/java/io/iztec/tp/integration/tns/sim/`

```java
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TnsSimConsumptionSnapshotId implements Serializable {

    @Column(name = "sim_id", nullable = false)
    private Integer simId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
```

---

### Step 7 — JPA entity `TnsSimConsumptionSnapshot`

Location: `integration-tns/src/main/java/io/iztec/tp/integration/tns/sim/`

- Does **not** extend `BaseEntity` (no UUID surrogate, no audit timestamps)
- `@Table(schema = "tns", name = "tns_sim_consumption_snapshot")`
- `@EmbeddedId TnsSimConsumptionSnapshotId id`
- All consumption columns mapped from `TnsSimResponse`

---

### Step 8 — Repository `TnsSimConsumptionSnapshotRepository`

Location: `integration-tns/src/main/java/io/iztec/tp/integration/tns/sim/`

- Extends `JpaRepository<TnsSimConsumptionSnapshot, TnsSimConsumptionSnapshotId>`
- Native `@Query` upsert:

```sql
INSERT INTO tns.tns_sim_consumption_snapshot
    (sim_id, recorded_at, iccid, msisdn, soldplan_id, soldplan_name,
     soldplan_consumption_f, excess_consumption_f, line_total_f)
VALUES
    (:simId, :recordedAt, :iccid, :msisdn, :soldplanId, :soldplanName,
     :soldplanConsumptionF, :excessConsumptionF, :lineTotalF)
ON CONFLICT (sim_id, recorded_at)
DO UPDATE SET
    iccid                  = EXCLUDED.iccid,
    msisdn                 = EXCLUDED.msisdn,
    soldplan_id            = EXCLUDED.soldplan_id,
    soldplan_name          = EXCLUDED.soldplan_name,
    soldplan_consumption_f = EXCLUDED.soldplan_consumption_f,
    excess_consumption_f   = EXCLUDED.excess_consumption_f,
    line_total_f           = EXCLUDED.line_total_f;
```

- Derived query for future time-range lookups:
  `findByIdSimIdAndIdRecordedAtBetween(Integer simId, Instant from, Instant to)`

---

### Step 9 — `TnsSimSnapshotService`

Location: `integration-tns/src/main/java/io/iztec/tp/integration/tns/sim/`

- Inject `TnsSimService` + `TnsSimConsumptionSnapshotRepository`
- Capture a single `Instant.now()` (UTC) for the whole batch
- Map each `TnsSimResponse` → `TnsSimConsumptionSnapshot` using the shared `recordedAt`
- Call the upsert for each snapshot (or batch via `saveAll` if upsert is adapted)

---

### Step 10 — `TnsSimSnapshotScheduler`

Location: `integration-tns/src/main/java/io/iztec/tp/integration/tns/sim/`

```java
@Component
@RequiredArgsConstructor
public class TnsSimSnapshotScheduler {

    private final TnsSimSnapshotService snapshotService;

    @Scheduled(fixedRate = 600_000)
    public void captureSnapshot() {
        snapshotService.captureAndPersist();
    }
}
```

---

### Step 11 — Enable scheduling on `TnsApplication`

File: `integration-tns/src/main/java/io/iztec/tp/integration/tns/TnsApplication.java`

Add `@EnableScheduling` to the class.

---

## Future time-bucket query example

```sql
-- MB consumed per SIM per hour (delta of cumulative value)
SELECT
    sim_id,
    time_bucket('1 hour', recorded_at) AS bucket,
    (MAX(soldplan_consumption_f) - MIN(soldplan_consumption_f)) / 1048576.0 AS mb_consumed
FROM tns.tns_sim_consumption_snapshot
WHERE recorded_at BETWEEN :from AND :to
GROUP BY sim_id, bucket
ORDER BY sim_id, bucket;
```

Replace `'1 hour'` with `'1 day'` or `'7 days'` for other aggregation windows.

