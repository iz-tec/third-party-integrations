package io.iztec.tp.integration.tns.sim;

import io.iztec.tp.integration.tns.auth.TnsAuthService;
import io.iztec.tp.integration.tns.config.DataSizeFloatDeserializer;
import io.iztec.tp.integration.tns.config.TnsProperties;
import io.iztec.tp.integration.tns.config.TnsRestClientConfig;
import io.iztec.tp.integration.tns.dto.sim.TnsSimPatchRequest;
import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-API e2e test for TnsSimService.
 *
 * Requires the following environment variables to be set:
 *   TNS_USERNAME — your TNS username
 *   TNS_PASSWORD — your TNS password
 *   TNS_BASE_URL — (optional) defaults to https://api.lsm-dev.tnsi.com.br
 *
 * Run with:
 *   TNS_USERNAME=xxx TNS_PASSWORD=yyy \
 *   mvn -pl integration-tns -am test -Dtest=TnsSimServiceE2ETest \
 *       -Dsurefire.failIfNoSpecifiedTests=false
 */
@SpringBootTest(
        classes = {TnsSimService.class, TnsAuthService.class, TnsRestClientConfig.class, DataSizeFloatDeserializer.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableConfigurationProperties(TnsProperties.class)
@ActiveProfiles("integration-test")
class TnsSimServiceE2ETest {

    @Autowired
    private TnsSimService simService;

    @Test
    void listSims_shouldReturnNonEmptyList() {
        List<TnsSimResponse> sims = simService.listSims();

        assertThat(sims)
                .as("TNS API should return at least one SIM")
                .isNotEmpty();

        System.out.println("Total SIMs returned: " + sims.size());
    }

    @Test
    void listSims_eachSim_shouldHaveRequiredFields() {
        List<TnsSimResponse> sims = simService.listSims();

        assertThat(sims).allSatisfy(sim -> {
            assertThat(sim.id()).as("id must not be null").isNotNull();
            assertThat(sim.iccid()).as("iccid must not be blank").isNotBlank();
            assertThat(sim.statusId()).as("statusId must not be null").isNotNull();
            assertThat(sim.phaseId()).as("phaseId must not be null").isNotNull();
        });
    }

    @Test
    void listSims_dataSizeFields_shouldBeNonNegativeBytes() {
        List<TnsSimResponse> sims = simService.listSims();

        assertThat(sims).allSatisfy(sim -> {
            if (sim.lineTotalF() != null) {
                assertThat(sim.lineTotalF())
                        .as("lineTotalF must be >= 0 bytes for SIM id=%d", sim.id())
                        .isGreaterThanOrEqualTo(0f);
            }
            if (sim.soldplanConsumptionF() != null) {
                assertThat(sim.soldplanConsumptionF())
                        .as("soldplanConsumptionF must be >= 0 bytes for SIM id=%d", sim.id())
                        .isGreaterThanOrEqualTo(0f);
            }
            if (sim.excessConsumptionF() != null) {
                assertThat(sim.excessConsumptionF())
                        .as("excessConsumptionF must be >= 0 bytes for SIM id=%d", sim.id())
                        .isGreaterThanOrEqualTo(0f);
            }
        });
    }

    @Test
    void getSimById_shouldReturnCorrectSim() {
        // grab the first SIM from the list to use as a known good ID
        List<TnsSimResponse> sims = simService.listSims();
        assertThat(sims).isNotEmpty();

        TnsSimResponse first = sims.get(0);
        TnsSimResponse fetched = simService.getSimById(first.id());

        assertThat(fetched).isNotNull();
        assertThat(fetched.id()).isEqualTo(first.id());
        assertThat(fetched.iccid()).isEqualTo(first.iccid());

        System.out.println("SIM fetched by id: id=" + fetched.id() + " iccid=" + fetched.iccid());
    }

    @Test
    @Disabled("Requires a TNS user with PATCH permission on /partners/sims/. " +
              "The current staging user (cli_simer) only has read access — TNS returns 403.")
    void patchSim_shouldTogglePhaseAndRestoreIt() {
        List<TnsSimResponse> sims = simService.listSims();
        assertThat(sims).isNotEmpty();

        TnsSimResponse original = sims.get(0);
        Integer originalPhaseId = original.phaseId();

        // pick a different phase to patch to (15 = blocked, 1 = linked)
        Integer targetPhaseId = originalPhaseId.equals(1) ? 15 : 1;

        TnsSimResponse patched = simService.patchSim(original.id(), new TnsSimPatchRequest(targetPhaseId));
        assertThat(patched.phaseId())
                .as("Phase should be updated to %d", targetPhaseId)
                .isEqualTo(targetPhaseId);

        // restore original phase
        TnsSimResponse restored = simService.patchSim(original.id(), new TnsSimPatchRequest(originalPhaseId));
        assertThat(restored.phaseId())
                .as("Phase should be restored to %d", originalPhaseId)
                .isEqualTo(originalPhaseId);

        System.out.println("SIM id=" + original.id() + " phase toggled and restored to " + originalPhaseId);
    }
}

