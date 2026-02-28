package io.iztec.tp.integration.tns.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned after a TNS notification is dispatched.
 */
public record TnsNotificationResponse(
        UUID eventId,
        String recipient,
        String status,
        Instant occurredAt
) {}

