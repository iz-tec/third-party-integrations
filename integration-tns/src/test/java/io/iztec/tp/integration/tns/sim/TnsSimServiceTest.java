package io.iztec.tp.integration.tns.sim;

import io.iztec.tp.integration.tns.config.TnsProperties;
import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TnsSimService.listAllSims() pagination logic.
 * The service is spied so that listSims() can be stubbed while listAllSims()
 * exercises its real loop logic.
 */
@ExtendWith(MockitoExtension.class)
class TnsSimServiceTest {

    @Mock
    private RestClient tnsRestClient;

    private TnsProperties tnsProperties;
    private TnsSimService simService;

    @BeforeEach
    void setUp() {
        tnsProperties = new TnsProperties();
        tnsProperties.setBaseUrl("http://fake-tns");
        tnsProperties.setUsername("fake-user");
        tnsProperties.setPassword("fake-pass");
        tnsProperties.setSimPageSize(100);
        tnsProperties.setSimMaxPages(10);

        simService = spy(new TnsSimService(tnsRestClient, tnsProperties));
    }

    /**
     * Builds a minimal TnsSimResponse fixture with only id and iccid set.
     */
    private TnsSimResponse fakeSim(int id) {
        return new TnsSimResponse(
                id, "iccid-" + id, null, null, null,
                null, null, null, null, null,
                null, null, null,
                null, null,
                null, null, null, null,
                null, null, null, null,
                null, null,
                null, null, null, null, null,
                null, null, null,
                null, null, null,
                null
        );
    }

    /**
     * Builds a list of N fake SIMs with sequential IDs starting from startId.
     */
    private List<TnsSimResponse> fakeSims(int startId, int count) {
        return IntStream.range(startId, startId + count)
                .mapToObj(this::fakeSim)
                .toList();
    }

    @Test
    void listAllSims_singleShortPage_shouldReturnAllAndStopAfterOnePage() {
        // API returns 50 SIMs (less than page size of 100) → only one call
        doReturn(fakeSims(1, 50)).when(simService).listSims(100, 0);

        List<TnsSimResponse> result = simService.listAllSims();

        assertThat(result).hasSize(50);
        verify(simService, times(1)).listSims(anyInt(), anyInt());
    }

    @Test
    void listAllSims_emptyFirstPage_shouldReturnEmptyList() {
        doReturn(Collections.emptyList()).when(simService).listSims(100, 0);

        List<TnsSimResponse> result = simService.listAllSims();

        assertThat(result).isEmpty();
        verify(simService, times(1)).listSims(anyInt(), anyInt());
    }

    @Test
    void listAllSims_oneFullPageThenShortPage_shouldReturnAll() {
        // First call returns exactly 100 (full page), second returns 30 (short → stop)
        doReturn(fakeSims(1, 100)).when(simService).listSims(100, 0);
        doReturn(fakeSims(101, 30)).when(simService).listSims(100, 100);

        List<TnsSimResponse> result = simService.listAllSims();

        assertThat(result).hasSize(130);
        verify(simService, times(2)).listSims(anyInt(), anyInt());
    }

    @Test
    void listAllSims_multipleFullPagesThenShortPage_shouldReturnAll() {
        // 3 full pages of 100 + 1 short page of 20 = 320 total
        doReturn(fakeSims(1, 100)).when(simService).listSims(100, 0);
        doReturn(fakeSims(101, 100)).when(simService).listSims(100, 100);
        doReturn(fakeSims(201, 100)).when(simService).listSims(100, 200);
        doReturn(fakeSims(301, 20)).when(simService).listSims(100, 300);

        List<TnsSimResponse> result = simService.listAllSims();

        assertThat(result).hasSize(320);
        verify(simService, times(4)).listSims(anyInt(), anyInt());
    }

    @Test
    void listAllSims_shouldStopAtMaxPagesGuard() {
        // Every page returns exactly 100 → loop hits the safety cap (10 pages)
        for (int i = 0; i < 10; i++) {
            doReturn(fakeSims(i * 100 + 1, 100)).when(simService).listSims(100, i * 100);
        }

        List<TnsSimResponse> result = simService.listAllSims();

        assertThat(result).hasSize(1000);
        // Should have called listSims exactly 10 times (the max-pages cap)
        verify(simService, times(10)).listSims(anyInt(), anyInt());
    }

    @Test
    void listAllSims_shouldRespectCustomPageSizeAndMaxPages() {
        // Override config to page size 5 and max pages 3
        tnsProperties.setSimPageSize(5);
        tnsProperties.setSimMaxPages(3);

        doReturn(fakeSims(1, 5)).when(simService).listSims(5, 0);
        doReturn(fakeSims(6, 5)).when(simService).listSims(5, 5);
        doReturn(fakeSims(11, 2)).when(simService).listSims(5, 10);

        List<TnsSimResponse> result = simService.listAllSims();

        assertThat(result).hasSize(12);
        verify(simService, times(3)).listSims(anyInt(), anyInt());
    }
}

