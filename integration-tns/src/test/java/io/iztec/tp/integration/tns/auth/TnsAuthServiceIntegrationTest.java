package io.iztec.tp.integration.tns.auth;

import io.iztec.tp.integration.tns.config.TnsProperties;
import io.iztec.tp.integration.tns.config.TnsRestClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real-API integration test for TnsAuthService.
 *
 * Requires the following environment variables to be set:
 *   TNS_USERNAME — your TNS username
 *   TNS_PASSWORD — your TNS password
 *   TNS_BASE_URL — (optional) defaults to https://api.lsm-dev.tnsi.com.br
 *
 * Run with:
 *   TNS_USERNAME=xxx TNS_PASSWORD=yyy \
 *   mvn -pl integration-tns -am test -Dtest=TnsAuthServiceIntegrationTest \
 *       -Dsurefire.failIfNoSpecifiedTests=false
 */
@SpringBootTest(
        classes = {TnsAuthService.class, TnsRestClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnableConfigurationProperties(TnsProperties.class)
@ActiveProfiles("integration-test")
class TnsAuthServiceIntegrationTest {

    @Autowired
    private TnsAuthService authService;

    @Test
    void getValidToken_shouldReturnNonBlankToken() {
        String token = authService.getValidToken();

        assertThat(token)
                .as("TNS API should return a non-blank JWT token")
                .isNotBlank();

        System.out.println("Token received (first 20 chars): "
                + token.substring(0, Math.min(20, token.length())) + "...");
    }

    @Test
    void getValidToken_calledTwice_shouldReturnCachedToken() {
        String first = authService.getValidToken();
        String second = authService.getValidToken();

        assertThat(first)
                .as("Second call should return the same cached token")
                .isEqualTo(second);
    }
}
