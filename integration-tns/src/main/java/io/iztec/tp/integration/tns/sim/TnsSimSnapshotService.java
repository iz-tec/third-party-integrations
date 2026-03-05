package io.iztec.tp.integration.tns.sim;

import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Fetches the full SIM list from the TNS API and persists a consumption snapshot
 * for each SIM using a single shared recorded_at timestamp (UTC) for the whole batch.
 *
 * Called by {@link TnsSimSnapshotScheduler} every 10 minutes.
 */
@Service
@RequiredArgsConstructor
public class TnsSimSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(TnsSimSnapshotService.class);

    private final TnsSimService simService;
    private final TnsSimConsumptionSnapshotRepository snapshotRepository;

    /**
     * Fetches all SIMs from TNS, then upserts one snapshot row per SIM.
     * All rows in the batch share the same recorded_at so they form a coherent tick.
     */
    public void captureAndPersist() {
        Instant recordedAt = Instant.now();
        log.info("Capturing SIM consumption snapshot at {}", recordedAt);

        List<TnsSimResponse> sims = simService.listAllSims();
        log.debug("Fetched {} SIMs from TNS", sims.size());

        for (TnsSimResponse sim : sims) {
            snapshotRepository.upsert(
                    sim.id(),
                    recordedAt,
                    sim.iccid(),
                    sim.msisdn(),
                    sim.soldplanId(),
                    sim.soldplanName(),
                    sim.soldplanConsumptionF(),
                    sim.excessConsumptionF(),
                    sim.lineTotalF()
            );
        }

        log.info("Snapshot persisted: {} SIM rows at {}", sims.size(), recordedAt);
    }
}

