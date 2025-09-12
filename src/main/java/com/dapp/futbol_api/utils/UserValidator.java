package com.dapp.futbol_api.utils;

import com.dapp.futbol_api.repositories.UserRepository;
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


    public void validateRegistrationRequest(RegisterRequest request) {
        // 1. Validación del formato del email
        if (request.getEmail() == null || !Pattern.matches(EMAIL_REGEX, request.getEmail())) {
            throw new IllegalArgumentException("The provided email format is not valid.");
        }

        // 2. Verificación de que el email no esté ya en uso
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("The email is already registered.");
        }

        // 3. Validación de la contraseña
        if (request.getPassword() == null || !Pattern.matches(PASSWORD_REGEX, request.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 6 characters long and contain at least one number.");
        }
    }
}
