package io.iztec.tp.integration.tns.exception;

import io.iztec.tp.commons.core.exception.ExternalApiException;

/**
 * Thrown when a TNS SIM API call returns a non-2xx response.
 */
public class TnsSimException extends ExternalApiException {

    public TnsSimException(int statusCode, String message) {
        super("tns", statusCode, message);
    }
}

