package io.iztec.tp.commons.database.repository;

import io.iztec.tp.commons.database.entity.IntegrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, UUID> {

    // Find all events for a given integration (e.g. "stripe")
    List<IntegrationEvent> findByIntegrationName(String integrationName);

    // Time-range query - leverages the TimescaleDB hypertable index on occurred_at
    List<IntegrationEvent> findByIntegrationNameAndOccurredAtBetween(
            String integrationName,
            Instant from,
            Instant to
    );
}

