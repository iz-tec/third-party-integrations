package io.iztec.tp.integration.tns.sim;

import io.iztec.tp.integration.tns.config.TnsProperties;
import io.iztec.tp.integration.tns.dto.sim.TnsSimPatchRequest;
import io.iztec.tp.integration.tns.dto.sim.TnsSimResponse;
import io.iztec.tp.integration.tns.exception.TnsSimException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Consumes the TNS SIM API endpoints:
 * - GET  /partners/sims/          — list all SIMs
 * - GET  /partners/sims/<pk>/     — get a single SIM by ID
 * - PATCH /partners/sims/<pk>/    — update a SIM (e.g. block/unblock via phaseId)
 */
@Service
public class TnsSimService {

    private static final Logger log = LoggerFactory.getLogger(TnsSimService.class);
    private static final String SIMS_PATH = "/partners/sims/";

    private final RestClient tnsRestClient;
    private final TnsProperties tnsProperties;

    public TnsSimService(@Qualifier("tnsRestClient") RestClient tnsRestClient,
                         TnsProperties tnsProperties) {
        this.tnsRestClient = tnsRestClient;
        this.tnsProperties = tnsProperties;
    }

    /**
     * Returns a paginated list of SIMs available to the partner.
     *
     * @param limit  max number of results to return (null = no limit sent to TNS)
     * @param offset number of results to skip (null = no offset sent to TNS)
     */
    public List<TnsSimResponse> listSims(Integer limit, Integer offset) {
        log.debug("Fetching SIMs from TNS (limit={}, offset={})", limit, offset);
        try {
            return tnsRestClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(SIMS_PATH);
                        if (limit != null)  builder = builder.queryParam("limit",  limit);
                        if (offset != null) builder = builder.queryParam("offset", offset);
                        return builder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TnsSimResponse>>() {});
        } catch (HttpClientErrorException e) {
            throw new TnsSimException(e.getStatusCode().value(),
                    "TNS SIM list failed (client error): " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new TnsSimException(e.getStatusCode().value(),
                    "TNS SIM list failed (server error): " + e.getResponseBodyAsString());
        }
    }

    /**
     * Fetches all SIMs by paginating through the TNS API.
     * Uses {@code tns.sim-page-size} as the page size and stops when:
     * <ul>
     *   <li>a page returns fewer results than the page size, or</li>
     *   <li>the safety cap of {@code tns.sim-max-pages} iterations is reached.</li>
     * </ul>
     */
    public List<TnsSimResponse> listAllSims() {
        int pageSize = tnsProperties.getSimPageSize();
        int maxPages = tnsProperties.getSimMaxPages();

        List<TnsSimResponse> allSims = new ArrayList<>();
        int page = 0;

        while (page < maxPages) {
            int offset = page * pageSize;
            List<TnsSimResponse> batch = listSims(pageSize, offset);
            allSims.addAll(batch);
            page++;

            if (batch.size() < pageSize) {
                log.debug("Last page reached (page={}, returned={})", page, batch.size());
                break;
            }
        }

        if (page >= maxPages) {
            log.warn("Reached max-pages safety cap ({} pages, {} SIMs fetched). "
                     + "There may be more SIMs — consider increasing tns.sim-max-pages.",
                     maxPages, allSims.size());
        }

        log.info("listAllSims completed: {} SIMs fetched in {} page(s)", allSims.size(), page);
        return allSims;
    }

    /**
     * Returns a single SIM by its ID.
     */
    public TnsSimResponse getSimById(Integer id) {
        log.debug("Fetching SIM id={} from TNS", id);
        try {
            return tnsRestClient.get()
                    .uri(SIMS_PATH + id + "/")
                    .retrieve()
                    .body(TnsSimResponse.class);
        } catch (HttpClientErrorException e) {
            throw new TnsSimException(e.getStatusCode().value(),
                    "TNS SIM get failed (client error): " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new TnsSimException(e.getStatusCode().value(),
                    "TNS SIM get failed (server error): " + e.getResponseBodyAsString());
        }
    }

    /**
     * Partially updates a SIM (e.g. block: phaseId=15 / unblock: phaseId=1).
     *
     * @deprecated Not available yet — requires a TNS user with write permission.
     */
    public TnsSimResponse patchSim(Integer id, TnsSimPatchRequest request) {
        throw new UnsupportedOperationException("PATCH /partners/sims/ is not enabled yet");
    }
}

