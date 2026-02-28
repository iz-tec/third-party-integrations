package io.iztec.tp.commons.database.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration for the common-database module.
 * Integration modules only need to import this config (or rely on Spring Boot auto-scan)
 * to get JPA and repositories enabled pointing at the shared packages.
 */
@Configuration
@EntityScan(basePackages = "io.iztec.tp.commons.database.entity")
@EnableJpaRepositories(basePackages = "io.iztec.tp.commons.database.repository")
public class DatabaseAutoConfiguration {
    // Spring Boot wires DataSource, EntityManagerFactory and TransactionManager automatically.
    // TimescaleDB-specific hypertables are created via Flyway migrations (see resources/db/migration).
}

