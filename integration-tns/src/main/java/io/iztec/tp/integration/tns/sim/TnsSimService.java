package io.iztec.tp.integration.tns.sim;

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

    public TnsSimService(@Qualifier("tnsRestClient") RestClient tnsRestClient) {
        this.tnsRestClient = tnsRestClient;
    }

    /**
     * Returns the full list of SIMs available to the partner.
     */
    public List<TnsSimResponse> listSims() {
        log.debug("Fetching all SIMs from TNS");
        try {
            return tnsRestClient.get()
                    .uri(SIMS_PATH)
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

