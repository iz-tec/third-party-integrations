package io.iztec.tp.commons.core.exception;

/**
 * Thrown when an integration-specific business rule is violated.
 * Each integration module can subclass this for more specific errors.
 */
public class IntegrationException extends RuntimeException {

    private final String integrationName;

    public IntegrationException(String integrationName, String message) {
        super(message);
        this.integrationName = integrationName;
    }

    public IntegrationException(String integrationName, String message, Throwable cause) {
        super(message, cause);
        this.integrationName = integrationName;
    }

    public String getIntegrationName() {
        return integrationName;
    }
}

