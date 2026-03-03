package io.iztec.tp.integration.tns.auth;

import io.iztec.tp.integration.tns.config.TnsProperties;
import io.iztec.tp.integration.tns.dto.auth.TnsAuthResponse;
import io.iztec.tp.integration.tns.exception.TnsAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;

/**
 * Manages the TNS JWT token lifecycle:
 * - Fetches a token from POST /auth/obtain/
 * - Caches it for its TTL (300 s by default)
 * - Refreshes it automatically when it is within `tokenRefreshBeforeSeconds` of expiry
 *
 * Thread safety: token state is guarded by a synchronized block so that concurrent virtual
 * threads do not trigger multiple simultaneous refresh requests.
 */
@Service
public class TnsAuthService {

    private static final Logger log = LoggerFactory.getLogger(TnsAuthService.class);
    private static final String AUTH_PATH = "/api/token-auth/";

    private final RestClient rawClient;
    private final TnsProperties properties;

    // guarded by `this`
    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public TnsAuthService(TnsProperties properties) {
        this.properties = properties;
        // Raw client — no auth interceptor, used only for the token endpoint
        this.rawClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    /**
     * Package-private constructor for unit tests: accepts a pre-built RestClient mock
     * so that no real HTTP calls are made.
     */
    TnsAuthService(TnsProperties properties, RestClient rawClient) {
        this.properties = properties;
        this.rawClient = rawClient;
    }

    /**
     * Returns a valid JWT token, refreshing it if it is expired or about to expire.
     */
    public synchronized String getValidToken() {
        if (needsRefresh()) {
            log.debug("TNS token missing or about to expire — refreshing");
            cachedToken = fetchNewToken();
            tokenExpiresAt = Instant.now().plusSeconds(properties.getTokenTtlSeconds());
            log.debug("TNS token refreshed, expires at {}", tokenExpiresAt);
        }
        return cachedToken;
    }

    private boolean needsRefresh() {
        return cachedToken == null
                || Instant.now().isAfter(tokenExpiresAt.minusSeconds(properties.getTokenRefreshBeforeSeconds()));
    }

    private String fetchNewToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", properties.getUsername());
        form.add("password", properties.getPassword());
        try {
            TnsAuthResponse response = rawClient.post()
                    .uri(AUTH_PATH)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TnsAuthResponse.class);

            if (response == null || response.token() == null || response.token().isBlank()) {
                throw new TnsAuthException(200, "TNS auth endpoint returned an empty token");
            }

            return response.token();

        } catch (HttpClientErrorException e) {
            throw new TnsAuthException(e.getStatusCode().value(),
                    "TNS auth failed (client error): " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new TnsAuthException(e.getStatusCode().value(),
                    "TNS auth failed (server error): " + e.getResponseBodyAsString());
        }
    }
}

