package io.iztec.tp.integration.tns.auth;

import io.iztec.tp.integration.tns.config.TnsProperties;
import io.iztec.tp.integration.tns.exception.TnsAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Unit tests for TnsAuthService token-caching logic.
 *
 * Uses MockRestServiceServer to intercept HTTP calls — no real network,
 * no complex Mockito fluent-chain mocking needed.
 */
class TnsAuthServiceTest {

    private static final String BASE_URL = "http://fake-tns";
    private static final String AUTH_URL = "/api/token-auth/";
    private static final String VALID_TOKEN_JSON = "{\"token\":\"my-jwt-token\"}";

    private MockRestServiceServer mockServer;
    private TnsAuthService authService;

    @BeforeEach
    void setUp() {
        TnsProperties properties = new TnsProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setUsername("user");
        properties.setPassword("pass");
        properties.setTokenTtlSeconds(300);
        properties.setTokenRefreshBeforeSeconds(30);

        // Build a RestTemplate-backed RestClient so MockRestServiceServer can intercept
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestClient rawClient = RestClient.create(restTemplate);

        authService = new TnsAuthService(properties, rawClient);
    }

    @Test
    void getValidToken_firstCall_shouldFetchAndReturnToken() {
        mockServer.expect(requestTo(AUTH_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(VALID_TOKEN_JSON, MediaType.APPLICATION_JSON));

        String token = authService.getValidToken();

        assertThat(token).isEqualTo("my-jwt-token");
        mockServer.verify();
    }

    @Test
    void getValidToken_secondCall_shouldReturnCachedTokenWithoutNewHttpCall() {
        // Only one server expectation — second call should use cache
        mockServer.expect(requestTo(AUTH_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(VALID_TOKEN_JSON, MediaType.APPLICATION_JSON));

        String first = authService.getValidToken();
        String second = authService.getValidToken();

        assertThat(first).isEqualTo(second).isEqualTo("my-jwt-token");
        mockServer.verify(); // confirms the server was called exactly once
    }

    @Test
    void getValidToken_whenApiReturnsNullToken_shouldThrowTnsAuthException() {
        mockServer.expect(requestTo(AUTH_URL))
                .andRespond(withSuccess("{\"token\":null}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> authService.getValidToken())
                .isInstanceOf(TnsAuthException.class)
                .hasMessageContaining("empty token");
    }

    @Test
    void getValidToken_whenApiReturnsBlankToken_shouldThrowTnsAuthException() {
        mockServer.expect(requestTo(AUTH_URL))
                .andRespond(withSuccess("{\"token\":\"   \"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> authService.getValidToken())
                .isInstanceOf(TnsAuthException.class)
                .hasMessageContaining("empty token");
    }

    @Test
    void getValidToken_whenApiReturnsEmptyBody_shouldThrowTnsAuthException() {
        mockServer.expect(requestTo(AUTH_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> authService.getValidToken())
                .isInstanceOf(TnsAuthException.class)
                .hasMessageContaining("empty token");
    }

    @Test
    void getValidToken_whenApiReturns401_shouldThrowTnsAuthException() {
        mockServer.expect(requestTo(AUTH_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> authService.getValidToken())
                .isInstanceOf(TnsAuthException.class)
                .hasMessageContaining("client error");
    }

    @Test
    void getValidToken_whenApiReturns500_shouldThrowTnsAuthException() {
        mockServer.expect(requestTo(AUTH_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> authService.getValidToken())
                .isInstanceOf(TnsAuthException.class)
                .hasMessageContaining("server error");
    }
}
