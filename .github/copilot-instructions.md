# Copilot instructions for third-party-integrations

## Build, test, and lint commands
- Start the local PostgreSQL + TimescaleDB stack before running anything that touches the database:

  ```bash
  docker compose up -d
  ```

- Build every module from the parent aggregator:

  ```bash
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean install
  ```

- Run a single integration module locally (this example uses the existing `integration-tns` module):

  ```bash
  cd integration-tns
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run
  ```

- To execute a single test class, target the module and supply `-Dtest` (substitute `MyTest` for the real class or method):

  ```bash
  mvn -pl integration-tns -Dtest=MyTest test
  ```

- There is no dedicated lint tool; rely on Maven’s default validation (`mvn -pl <module> test` or `mvn -pl <module> verify`) when you need broader checks.

## High-level architecture
- The root `pom.xml` is a packaging-only aggregator that stitches together:
  - `common-core`: shared DTOs, exceptions, and utilities without Spring Web/Data.
  - `common-database`: JPA entities, repositories, Flyway migrations, and database helpers (no Web dependency).
  - `integration-tns` (and future `integration-<name>` modules): Spring Boot apps that depend on the shared modules and provide third-party integration APIs.

- Module dependency rules flow inward: each integration module depends on `common-database`, which depends on `common-core`. Only integration modules bring in Spring Web.
- The stack is Java 21 + Spring Boot 3.4.3, PostgreSQL (with TimescaleDB extensions) managed via the root `docker-compose.yml`, and Flyway migrations live under `common-database/src/main/resources/db/migration`.
- All documentation is centralized under `docs/`, with `docs/development_guideline.md` describing these architecture decisions and conventions.

## Key conventions
- **DTOs are Java records.** Request/response payloads (and other inter-module DTOs) reside under `io.iztec.tp.integration.<name>.dto` and must be declared as `public record ...`.
- **Entities extend `BaseEntity`.** That base class is responsible for UUID primary keys plus `createdAt`/`updatedAt` timestamps annotated with `@PrePersist`/`@PreUpdate`; all tables rely on those audit columns.
- **API responses use `ApiResponse<T>`** from `common-core`. Wrap every controller return value with `ApiResponse.ok(...)` for success or `ApiResponse.fail(...)` for errors before returning via `ResponseEntity`.
- **Exception hierarchy.** Start with the shared `IntegrationException` for business failures and subclass `ExternalApiException` (or more specific ones per integration) when third-party HTTP calls blow up.
- **Package layout.** Stick to `io.iztec.tp.commons.core.*`, `io.iztec.tp.commons.database.*`, and `io.iztec.tp.integration.<name>.*` so that module ownership stays explicit.
- **Database migration discipline.** Hibernate is configured with `ddl-auto: validate`; schema changes only arrive through Flyway scripts named `V{number}__{description}.sql`. The `integration_event` table is a TimescaleDB hypertable partitioned by `occurred_at`.
- **New integration checklist.** Duplicate `integration-sample/`, add the module to the parent `pom.xml`, rename Maven coordinates and Spring Boot settings, and keep DTOs as records. Depend on `common-database` only if you need persistence, otherwise stick to `common-core`.
