package com.dapp.futbol_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for errors occurring in the AnalysisService.
 * This will result in an HTTP 503 Service Unavailable status.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AnalysisServiceException extends RuntimeException {

    public AnalysisServiceException(String message) {
        super(message);
    }

    public AnalysisServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
