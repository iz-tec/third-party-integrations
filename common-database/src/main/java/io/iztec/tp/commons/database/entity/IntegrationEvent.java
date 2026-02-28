package io.iztec.tp.commons.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Records every event received from or sent to a third-party integration.
 *
 * This table is converted into a TimescaleDB hypertable (partitioned by "occurred_at")
 * via the Flyway migration V2__create_hypertables.sql, enabling efficient time-series queries.
 */
@Getter
@Setter
@Entity
@Table(name = "integration_event")
public class IntegrationEvent extends BaseEntity {

    // Which integration produced this event (e.g. "stripe", "twilio")
    @Column(name = "integration_name", nullable = false, length = 64)
    private String integrationName;

    // Logical event type (e.g. "PAYMENT_RECEIVED", "SMS_SENT")
    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    // Raw JSON payload from/to the third party
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    // When the event actually occurred at the source - used as TimescaleDB partition key
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}

