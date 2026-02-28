package io.iztec.tp.integration.tns.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for sending a TNS notification.
 */
public record TnsNotificationRequest(
        @NotBlank String recipient,
        @NotBlank String message
) {}

