package com.dapp.futbol_api.utils;

import com.dapp.futbol_api.security.AuthenticationRequest;
import com.dapp.futbol_api.security.RegisterRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserValidator {

    // Standard regular expression for email validation.
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    // Regular expression for the password: at least 6 characters and one number.
    private static final String PASSWORD_REGEX = "^(?=.*\\d).{6,}$";
    private static final int MAX_PASSWORD_LENGTH = 128;

    public UserValidator() {
    }

    public void validateRegistrationRequest(RegisterRequest request) {
        // Normalize email before validating
        request.setEmail(normalizeEmail(request.getEmail()));

        // Validate email format
        validateEmailFormat(request.getEmail());

        // Validate password format
        validatePasswordFormat(request.getPassword());
    }

    public void validateAuthenticationRequest(AuthenticationRequest request) {
        // Normalize email before validating
        request.setEmail(normalizeEmail(request.getEmail()));

        // Validate email format
        validateEmailFormat(request.getEmail());

        // Validate password format
        validatePasswordFormat(request.getPassword());
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private void validateEmailFormat(String email) {
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("The provided email format is invalid.");
        }
    }

    private void validatePasswordFormat(String password) {
        if (password != null && password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password cannot be longer than " + MAX_PASSWORD_LENGTH + " characters.");
        }

        if (password == null || !Pattern.matches(PASSWORD_REGEX, password)) {
            throw new IllegalArgumentException(
                    "Password must be at least 6 characters long and contain at least one digit.");
        }
    }
}