package io.iztec.tp.integration.tns.sim;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iztec.tp.integration.tns.dto.sim.TnsSimPatchRequest;
import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for TnsSimController.
 *
 * The nested @SpringBootApplication config replaces TnsApplication for this
 * test context, keeping @EnableJpaRepositories out of scope so no DataSource
 * or entityManagerFactory is required.
 */
@WebMvcTest(
        controllers = TnsSimController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class,
                SpringDataWebAutoConfiguration.class,
                FlywayAutoConfiguration.class,
                TransactionAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class
        }
)
@ActiveProfiles("test")
class TnsSimControllerTest {

    /**
     * Minimal @SpringBootApplication without @EnableJpaRepositories or @EntityScan.
     * @WebMvcTest finds this nested class first and uses it as the configuration
     * root instead of scanning up to TnsApplication.
     */
    @SpringBootApplication(scanBasePackages = {
            "io.iztec.tp.commons",
            "io.iztec.tp.integration.tns"
    })
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TnsSimService simService;

    @MockitoBean
    private TnsSimConsumptionSnapshotRepository snapshotRepository;

    // --- helper to build a minimal TnsSimResponse fixture ---

    private TnsSimResponse fakeSim(int id, String iccid) {
        return new TnsSimResponse(
                id, iccid, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null,
                1, "active", 1, "linked",
                null, null, null, null,
                null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null,
                null
        );
    }

    // --- GET /tns/sims ---

    @Test
    void listSims_shouldReturn200WithSuccessTrueAndSimList() throws Exception {
        TnsSimResponse sim1 = fakeSim(1, "89001");
        TnsSimResponse sim2 = fakeSim(2, "89002");
        when(simService.listSims(null, null)).thenReturn(List.of(sim1, sim2));

        mockMvc.perform(get("/tns/sims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[1].id", is(2)));
    }

    @Test
    void listSims_emptyList_shouldReturn200WithEmptyArray() throws Exception {
        when(simService.listSims(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/tns/sims"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void listSims_withLimitAndOffset_shouldForwardParamsToService() throws Exception {
        TnsSimResponse sim = fakeSim(5, "89005");
        when(simService.listSims(10, 2)).thenReturn(List.of(sim));

        mockMvc.perform(get("/tns/sims").param("limit", "10").param("offset", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));

        verify(simService).listSims(10, 2);
    }

    // --- GET /tns/sims/{id} ---

    @Test
    void getSimById_shouldReturn200WithSim() throws Exception {
        TnsSimResponse sim = fakeSim(42, "89055");
        when(simService.getSimById(42)).thenReturn(sim);

        mockMvc.perform(get("/tns/sims/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(42)))
                .andExpect(jsonPath("$.data.iccid", is("89055")));
    }

    // --- PATCH /tns/sims/{id} ---

    @Test
    void patchSim_shouldReturn501WithSuccessFalse() throws Exception {
        TnsSimPatchRequest request = new TnsSimPatchRequest(15);

        mockMvc.perform(patch("/tns/sims/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", containsString("not enabled")));

        verify(simService, never()).patchSim(anyInt(), any());
    }

    @Test
    void patchSim_withMissingBody_shouldReturn400() throws Exception {
        mockMvc.perform(patch("/tns/sims/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
