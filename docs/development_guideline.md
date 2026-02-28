# Development Guideline

This document describes the tech stack, architecture decisions, and coding conventions for the `third-party-integrations` platform.

---

## Tech Stack

| Layer | Technology | Version | Notes |
|---|---|---|---|
| Language | Java | 21 LTS | Virtual threads enabled via `--enable-preview` |
| Framework | Spring Boot | 3.4.3 | Web, Data JPA, Validation |
| Build | Maven | 3.x | Multi-module project |
| Database | PostgreSQL + TimescaleDB | PG 16 | Time-series hypertables for event storage |
| Migrations | Flyway | (managed by Spring Boot BOM) | PostgreSQL variant |
| ORM | Spring Data JPA / Hibernate | (managed by Spring Boot BOM) | `ddl-auto: validate` — Flyway owns the schema |
| Containerisation | Docker Compose | - | Local dev only; `docker-compose.yml` at root |

---

## Module Structure

```
third-party-integrations/          <- Parent POM (aggregator only, no code)
├── common-core/                   <- Shared DTOs, exceptions, utilities
├── common-database/               <- JPA entities, repositories, Flyway migrations
├── integration-<name>/            <- One module per third-party service
└── docs/                          <- All documentation lives here
```

### Dependency Rules

```
integration-<name>
    └── common-database
            └── common-core
```

- `common-core` has **no** Spring Web or Spring Data dependency — only Spring Context and Jackson.
- `common-database` has **no** Spring Web dependency.
- Only integration modules pull in `spring-boot-starter-web`.

---

## Coding Conventions

### DTOs must be Java Records

Every Data Transfer Object (request body, response body, or inter-module data carrier) **must** be declared as a Java `record`. No classes with getters/setters for DTOs.

```java
// CORRECT
public record CreateEventRequest(String eventType, String payload) {}

// WRONG — do not use classes for DTOs
public class CreateEventRequest {
    private String eventType;
    // ...
}
```

Records are immutable by default, require no Lombok, and make serialization with Jackson straightforward.

### Entities are JPA classes (exception to the record rule)

JPA entities **cannot** be records (JPA requires mutable state and a no-arg constructor). All entities must extend `BaseEntity`, which provides:
- `UUID` primary key (auto-generated)
- `createdAt` and `updatedAt` audit timestamps via `@PrePersist` / `@PreUpdate`

### API Responses

All REST endpoints must wrap their return value in `ApiResponse<T>` from `common-core`:

```java
return ResponseEntity.ok(ApiResponse.ok(myData));       // success
return ResponseEntity.ok(ApiResponse.fail("message"));  // error
```

### Exception Hierarchy

| Exception | When to use |
|---|---|
| `IntegrationException` | Base for any integration business-rule failure |
| `ExternalApiException` | Third-party HTTP call returned 4xx/5xx |

Subclass these in your integration module for more specific errors.

### Package Naming

```
io.iztec.tp.commons.core.*        <- common-core
io.iztec.tp.commons.database.*    <- common-database
io.iztec.tp.integration.<name>.*  <- per-integration module
```

---

## Database Conventions

- All tables have a `UUID` primary key and `created_at` / `updated_at` columns (inherited from `BaseEntity`).
- Time-series data (events) goes into the `integration_event` table, which is a **TimescaleDB hypertable** partitioned by `occurred_at` (7-day chunks).
- Schema changes are done **only via Flyway migrations** in `common-database/src/main/resources/db/migration/`. Hibernate never modifies the schema (`ddl-auto: validate`).
- Migration naming: `V{number}__{description}.sql` (e.g. `V3__add_status_to_event.sql`).

---

## Adding a New Integration

1. Copy `integration-sample/` to `integration-<name>/`.
2. Add `<module>integration-<name></module>` to the root `pom.xml`.
3. Rename the `artifactId`, `name`, package, `Application` class, and `spring.application.name` in `application.yml`.
4. All request/response DTOs go in `io.iztec.tp.integration.<name>.dto` as Java **records**.
5. If the integration needs database access, depend on `common-database`; otherwise depend on `common-core` only.

