package io.iztec.tp.integration.tns.config;

import io.iztec.tp.integration.tns.auth.TnsAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Produces a {@link RestClient} bean pre-configured with:
 * - TNS base URL
 * - A request interceptor that injects "Authorization: JWT <token>" on every outgoing call,
 *   fetching / refreshing the token via {@link TnsAuthService} as needed.
 *
 * Inject this bean (qualified as "tnsRestClient") in any service that needs to call the TNS API.
 */
@Configuration
public class TnsRestClientConfig {

    @Bean("tnsRestClient")
    public RestClient tnsRestClient(TnsProperties properties, TnsAuthService authService) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestInterceptor((request, body, execution) -> {
                    String token = authService.getValidToken();
                    request.getHeaders().set("Authorization", "JWT " + token);
                    return execution.execute(request, body);
                })
                .build();
    }
}

