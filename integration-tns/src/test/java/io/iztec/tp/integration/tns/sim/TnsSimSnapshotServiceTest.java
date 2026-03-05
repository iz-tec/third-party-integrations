package io.iztec.tp.integration.tns.sim;

import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TnsSimSnapshotService.
 * All dependencies are mocked — no DB, no real HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class TnsSimSnapshotServiceTest {

    @Mock
    private TnsSimService simService;

    @Mock
    private TnsSimConsumptionSnapshotRepository snapshotRepository;

    @InjectMocks
    private TnsSimSnapshotService snapshotService;

    /**
     * Builds a minimal TnsSimResponse for test fixtures.
     * Only id, iccid, and the three _f fields are set; everything else is null.
     */
    private TnsSimResponse buildSim(int id, String iccid, Float lineTotalF,
                                    Float soldplanConsumptionF, Float excessConsumptionF) {
        return new TnsSimResponse(
                // id, iccid, msisdn, imei, imeiLock
                id, iccid, null, null, null,
                // operadoraId, operatorName, typeId, typeName, lineId
                null, null, null, null, null,
                // lineTotalF, lineLastSuspension, lineEstimatedSuspensionEnd
                lineTotalF, null, null,
                // lastConn, lastDisc
                null, null,
                // statusId, statusName, phaseId, phaseName
                null, null, null, null,
                // soldplanId, soldplanName, soldplanBasePrice, soldplanConsumptionF
                null, null, null, soldplanConsumptionF,
                // soldplanGroupId, soldplanGroupName
                null, null,
                // customerId, customerName, customerTid, customerMid, customerCostCenter
                null, null, null, null, null,
                // itemId, contractId, chargeStart
                null, null, null,
                // replacesId, replacesIccid, details
                null, null, null,
                // excessConsumptionF
                excessConsumptionF
        );
    }

    @Test
    void captureAndPersist_withTwoSims_shouldCallUpsertTwice() {
        TnsSimResponse sim1 = buildSim(1, "89001", 1024f, 2048f, 0f);
        TnsSimResponse sim2 = buildSim(2, "89002", 512f, 1024f, 100f);
        when(simService.listSims(null, null)).thenReturn(List.of(sim1, sim2));

        snapshotService.captureAndPersist();

        verify(snapshotRepository, times(2)).upsert(
                anyInt(), any(Instant.class), anyString(),
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void captureAndPersist_withEmptyList_shouldNeverCallUpsert() {
        when(simService.listSims(null, null)).thenReturn(List.of());

        snapshotService.captureAndPersist();

        verify(snapshotRepository, never()).upsert(
                anyInt(), any(Instant.class), anyString(),
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void captureAndPersist_allRowsShareTheSameRecordedAt() {
        TnsSimResponse sim1 = buildSim(1, "89001", 1024f, 2048f, 0f);
        TnsSimResponse sim2 = buildSim(2, "89002", 512f, 1024f, 100f);
        when(simService.listSims(null, null)).thenReturn(List.of(sim1, sim2));

        snapshotService.captureAndPersist();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(snapshotRepository, times(2)).upsert(
                anyInt(), captor.capture(), anyString(),
                any(), any(), any(), any(), any(), any()
        );

        List<Instant> recordedAts = captor.getAllValues();
        assertThat(recordedAts).hasSize(2);
        assertThat(recordedAts.get(0))
                .as("Both SIMs in a batch must share the same recorded_at")
                .isEqualTo(recordedAts.get(1));
    }

    @Test
    void captureAndPersist_shouldPassCorrectSimIdAndIccid() {
        TnsSimResponse sim = buildSim(42, "89055555", 0f, 20971520f, 0f);
        when(simService.listSims(null, null)).thenReturn(List.of(sim));

        snapshotService.captureAndPersist();

        verify(snapshotRepository).upsert(
                eq(42), any(Instant.class), eq("89055555"),
                isNull(), isNull(), isNull(),
                eq(20971520f), eq(0f), eq(0f)
        );
    }
}
