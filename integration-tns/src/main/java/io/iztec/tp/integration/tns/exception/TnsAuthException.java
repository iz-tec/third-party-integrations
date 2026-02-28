package io.iztec.tp.integration.tns.exception;

import io.iztec.tp.commons.core.exception.ExternalApiException;

/**
 * Thrown when the TNS auth endpoint returns a non-2xx response or the token cannot be obtained.
 */
public class TnsAuthException extends ExternalApiException {

    public TnsAuthException(int statusCode, String message) {
        super("tns", statusCode, message);
    }
}

