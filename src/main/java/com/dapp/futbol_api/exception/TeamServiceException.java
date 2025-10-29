package com.dapp.futbol_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for errors occurring in the TeamService.
 * This will result in an HTTP 503 Service Unavailable status.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class TeamServiceException extends RuntimeException {

    public TeamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}