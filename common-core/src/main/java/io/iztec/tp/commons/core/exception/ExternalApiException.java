package io.iztec.tp.commons.core.exception;

/**
 * Thrown when an external third-party API call fails (e.g. HTTP 4xx/5xx from the remote service).
 */
public class ExternalApiException extends IntegrationException {

    private final int statusCode;

    public ExternalApiException(String integrationName, int statusCode, String message) {
        super(integrationName, message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

