package io.iztec.tp.integration.tns.sim;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * Composite primary key for {@link TnsSimConsumptionSnapshot}.
 *
 * The natural unique key for a snapshot batch is (sim_id, recorded_at):
 * - sim_id     — identifies the SIM card
 * - recorded_at — the UTC timestamp of the scheduler tick (same for all SIMs in a batch)
 *
 * TimescaleDB requires the partition column (recorded_at) to be part of the PK.
 */
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

