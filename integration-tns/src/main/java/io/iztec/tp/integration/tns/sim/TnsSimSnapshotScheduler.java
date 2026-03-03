package io.iztec.tp.integration.tns.sim;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers a SIM consumption snapshot every 10 minutes.
 *
 * fixedRate = 600_000 ms: fires every 10 minutes regardless of how long the previous
 * run took. If the API is slow and a run overlaps the next tick, Spring will queue
 * the next execution and run it immediately after the current one finishes (default
 * single-threaded task executor behaviour).
 */
@Component
@RequiredArgsConstructor
public class TnsSimSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(TnsSimSnapshotScheduler.class);

    private final TnsSimSnapshotService snapshotService;

    @Scheduled(fixedRate = 600_000)
    public void captureSnapshot() {
        log.debug("Scheduler triggered: capturing SIM consumption snapshot");
        snapshotService.captureAndPersist();
    }
}

