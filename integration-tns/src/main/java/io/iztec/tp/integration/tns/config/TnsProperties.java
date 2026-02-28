package io.iztec.tp.integration.tns.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Binds all properties under the "tns" prefix from application.yml / environment variables.
 * Password is excluded from toString to avoid leaking credentials in logs.
 */
@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "tns")
public class TnsProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String username;

    @NotBlank
    @ToString.Exclude
    private String password;

    @Positive
    private int tokenTtlSeconds = 300;

    @Positive
    private int tokenRefreshBeforeSeconds = 30;
}

