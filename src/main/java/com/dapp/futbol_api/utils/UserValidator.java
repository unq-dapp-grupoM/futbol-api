package com.dapp.futbol_api.utils;

import com.dapp.futbol_api.repositories.UserRepository;
import com.dapp.futbol_api.security.AuthenticationRequest;
import com.dapp.futbol_api.security.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository repository;

    // Expresión regular estándar para la validación de emails.
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
    // Expresión regular para la contraseña: al menos 6 caracteres y un número.
    private static final String PASSWORD_REGEX = "^(?=.*[0-9]).{6,}$";
    private static final int MAX_PASSWORD_LENGTH = 128;

    public void validateRegistrationRequest(RegisterRequest request) {
        // Normalizamos el email antes de validar
        request.setEmail(normalizeEmail(request.getEmail()));

        // Validación del formato del email
        validateEmailFormat(request.getEmail());

        // Verificación de que el email no esté ya en uso
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("The email is already registered.");
        }

        // Validación del formato de la contraseña
        validatePasswordFormat(request.getPassword());
    }

    public void validateAuthenticationRequest(AuthenticationRequest request) {
        // Normalizamos el email antes de validar
        request.setEmail(normalizeEmail(request.getEmail()));

        // Validación del formato del email
        validateEmailFormat(request.getEmail());

        // Validate the password format
        validatePasswordFormat(request.getPassword());

        // Verificación de que el usuario exista
        if (repository.findByEmail(request.getEmail()).isEmpty()) {
            throw new IllegalArgumentException("User with the provided email is not registered.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private void validateEmailFormat(String email) {
        if (email == null || !Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("The provided email format is not valid.");
        }
    }

    private void validatePasswordFormat(String password) {
        if (password != null && password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters.");
        }

        if (password == null || !Pattern.matches(PASSWORD_REGEX, password)) {
            throw new IllegalArgumentException(
                    "Password must be at least 6 characters long and contain at least one number.");
        }
    }
}
