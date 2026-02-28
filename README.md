# third-party-integrations

Central platform for all third-party service integrations at iztec.

## Documentation

All documentation lives in the [`docs/`](docs/) folder.

| Document | Description |
|---|---|
| [Development Guideline](docs/development_guideline.md) | Tech stack, architecture decisions, and coding conventions |

## Quick Start

Start the local database:

```bash
docker compose up -d
```

Build all modules:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean install
```

Run a specific integration:

```bash
cd integration-sample
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn spring-boot:run
```
