package io.iztec.tp.integration.tns.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TnsAuthRequest(
        @NotBlank String username,
        @NotBlank String password
) {}

