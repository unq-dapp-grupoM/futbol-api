package com.dapp.futbol_api.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void testHandleIllegalArgumentShouldReturnBadRequest() {
        // Arrange
        // Instantiate the handler for testing
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        // Create an exception with a specific message
        String errorMessage = "This is a test error message.";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act: Call the handler method
        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgument(exception);

        // Assert: Verify the response status and body
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}
