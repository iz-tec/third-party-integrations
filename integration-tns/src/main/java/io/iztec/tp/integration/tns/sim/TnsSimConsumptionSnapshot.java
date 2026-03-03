package io.iztec.tp.integration.tns.sim;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity that stores a point-in-time snapshot of a SIM card's consumption data.
 *
 * Mapped to the TimescaleDB hypertable tns.tns_sim_consumption_snapshot, partitioned
 * by recorded_at (1-day chunks). One row per SIM per scheduler tick (every 10 minutes).
 *
 * The _f fields (soldplanConsumptionF, excessConsumptionF, lineTotalF) are cumulative
 * values in bytes. Use MAX - MIN within a time_bucket window to compute actual consumption
 * over a period (hour / day / 7 days).
 *
 * Does NOT extend BaseEntity: no UUID surrogate or audit timestamps are needed here.
 * The composite PK (sim_id, recorded_at) is the natural identity for this time-series data.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "tns", name = "tns_sim_consumption_snapshot")
public class TnsSimConsumptionSnapshot {

    @EmbeddedId
    private TnsSimConsumptionSnapshotId id;

    // Human-readable SIM identifier
    @Column(name = "iccid", length = 22)
    private String iccid;

    // Phone number assigned to this SIM
    @Column(name = "msisdn", length = 32)
    private String msisdn;

    // Sold plan identifier
    @Column(name = "soldplan_id")
    private Integer soldplanId;

    // Sold plan name
    @Column(name = "soldplan_name", length = 128)
    private String soldplanName;

    // Cumulative data consumed within the plan quota, in bytes (from soldplan__consumption_f)
    @Column(name = "soldplan_consumption_f")
    private Float soldplanConsumptionF;

    // Cumulative data consumed beyond the plan quota, in bytes (from excess_consumption_f)
    @Column(name = "excess_consumption_f")
    private Float excessConsumptionF;

    // Cumulative total line consumption, in bytes (from line__total_f)
    @Column(name = "line_total_f")
    private Float lineTotalF;
}

