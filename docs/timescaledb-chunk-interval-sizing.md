# TimescaleDB Chunk Interval Sizing

## What is `chunk_time_interval`?

TimescaleDB physically partitions a hypertable into **chunks** — each chunk is an internal
table covering a specific time range. `chunk_time_interval` defines the width of that range.

It has **no effect on query results or aggregation accuracy**. It only affects:
- How much data is scanned per query (chunk pruning)
- Memory pressure during queries
- Chunk management overhead

---

## What it does NOT affect

- `time_bucket('1 hour', recorded_at)` returns correct hourly data regardless of chunk size
- Any aggregation window (1 min, 6 hours, 7 days, etc.) works freely
- Individual row accuracy — every snapshot is stored as-is

---

## Sizing rule of thumb (TimescaleDB recommendation)

Target **25–50 million rows per chunk** across all ingestion sources.

```
chunk_interval = target_rows / (rows_per_second)

rows_per_second = number_of_records_per_tick / tick_interval_in_seconds
```

---

## Reference table

| Records/tick | Tick interval | rows/sec | 1-day rows | Recommended chunk | Notes |
|---|---|---|---|---|---|
| 150 SIMs | 10 min (600s) | 0.25 | 21,600 | `1 day` ✅ current | Well within range |
| 150 SIMs | 1 min | 2.5 | 216,000 | `1 day` | Still fine |
| 150 SIMs | 1 sec | 150 | 12,960,000 | `2–3 days` | Approaching limit |
| 1,000 SIMs | 1 sec | 1,000 | 86,400,000 | `8–12 hours` | Reduce chunk size |
| 100 rec/s (any) | — | 100 | 8,640,000 | `3 days` | Sweet spot at 25M target |

---

## Current project configuration

| Parameter | Value | Rationale |
|---|---|---|
| SIM cards | 150 | Expected fleet size as of 2026-03-03 |
| Ingestion cadence | every 10 minutes | `@Scheduled(fixedRate = 600_000)` |
| rows/sec | 0.25 (150 / 600) | Very low ingestion rate |
| rows/day | ~21,600 | 150 SIMs × 144 ticks/day |
| `chunk_time_interval` | `1 day` | Correct — each chunk holds ~21k rows, well below the 25M ceiling |

At the current rate, the `1 day` chunk interval would only need revisiting if:
- The SIM fleet grows beyond ~170,000 cards at 10-min cadence, or
- The ingestion cadence drops to 1 second or below

---

## When to revisit

Use this formula to recalculate when fleet size or cadence changes:

```
rows_per_day   = number_of_sims × ticks_per_day
ticks_per_day  = 86,400 / tick_interval_in_seconds

optimal_chunk_days = 25,000,000 / rows_per_day
```

Example — if fleet grows to 5,000 SIMs at 10-min cadence:

```
rows_per_day  = 5,000 × 144 = 720,000
optimal_chunk = 25,000,000 / 720,000 ≈ 34 days  →  INTERVAL '30 days'
```

Update `chunk_time_interval` in the Flyway migration and apply via:

```sql
SELECT set_chunk_time_interval('tns.tns_sim_consumption_snapshot', INTERVAL '30 days');
```

No data loss, no downtime — TimescaleDB applies it to new chunks only.

---

## Chunk interval vs query window relationship

A query for `time_bucket('1 hour', recorded_at)` over the last 24 hours with `1 day` chunks
touches exactly **1 chunk**. TimescaleDB skips all other chunks — this is the core performance
benefit (chunk exclusion).

```
query window <= chunk_interval  →  1 chunk scanned  (best case)
query window  > chunk_interval  →  N chunks scanned  (still efficient, pruning still applies)
```

Smaller chunk = finer pruning = better for narrow time-range queries.
Larger chunk = fewer chunks to manage = better for wide scans and compression.

