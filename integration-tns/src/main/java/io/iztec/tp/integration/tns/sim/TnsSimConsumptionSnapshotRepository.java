package io.iztec.tp.integration.tns.sim;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface TnsSimConsumptionSnapshotRepository
        extends JpaRepository<TnsSimConsumptionSnapshot, TnsSimConsumptionSnapshotId> {

    /**
     * Upserts a single snapshot row.
     * On conflict (same sim_id + recorded_at) all consumption columns are overwritten
     * with the latest values from the TNS API response.
     */
    @Modifying
    @Transactional
    @Query(value = """
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
                line_total_f           = EXCLUDED.line_total_f
            """, nativeQuery = true)
    void upsert(
            @Param("simId") Integer simId,
            @Param("recordedAt") Instant recordedAt,
            @Param("iccid") String iccid,
            @Param("msisdn") String msisdn,
            @Param("soldplanId") Integer soldplanId,
            @Param("soldplanName") String soldplanName,
            @Param("soldplanConsumptionF") Float soldplanConsumptionF,
            @Param("excessConsumptionF") Float excessConsumptionF,
            @Param("lineTotalF") Float lineTotalF
    );

    /**
     * Returns all snapshots for a given SIM within a time range.
     * Used for future time-bucket aggregation queries (hourly / daily / 7-day).
     */
    List<TnsSimConsumptionSnapshot> findByIdSimIdAndIdRecordedAtBetween(
            Integer simId,
            Instant from,
            Instant to
    );
}

