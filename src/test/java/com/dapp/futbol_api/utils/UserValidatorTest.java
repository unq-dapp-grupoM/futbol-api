package com.dapp.futbol_api.utils;

import com.dapp.futbol_api.model.User;
import com.dapp.futbol_api.repositories.UserRepository;
import com.dapp.futbol_api.security.AuthenticationRequest;
import com.dapp.futbol_api.security.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserValidatorTest {
    

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    @Test
    void testValidateRegistrationRequestShouldPassWithValidData() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123");
        // Mock repository to indicate email is not in use
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert: No exception should be thrown
        assertDoesNotThrow(() -> userValidator.validateRegistrationRequest(registerRequest));

        // Verify that the repository was checked with the normalized email
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForExistingEmail() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password123");
        // Mock repository to indicate email is already in use
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        // Act & Assert: Expect an IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("The email is already in use.", exception.getMessage());
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForInvalidEmailFormat() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("invalid-email", "password123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("The provided email format is invalid.", exception.getMessage());
        // Verify repository is not called if email is invalid
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForShortPassword() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "pass"); // Less than 6 characters

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("Password must be at least 6 characters long and contain at least one digit.", exception.getMessage());
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForPasswordWithoutDigit() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "password");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("Password must be at least 6 characters long and contain at least one digit.", exception.getMessage());
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForNullEmail() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest(null, "password123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("The provided email format is invalid.", exception.getMessage());
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForNullPassword() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("Password must be at least 6 characters long and contain at least one digit.", exception.getMessage());
    }

    @Test
    void testValidateRegistrationRequestShouldThrowExceptionForTooLongPassword() {
        // Arrange
        String longPassword = "a".repeat(129);
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", longPassword);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateRegistrationRequest(registerRequest));

        assertEquals("Password cannot be longer than 128 characters.", exception.getMessage());
    }

    @Test
    void testValidateRegistrationRequestShouldNormalizeEmail() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest("  Test@Example.COM  ", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act
        userValidator.validateRegistrationRequest(registerRequest);

        // Assert: The email in the request object should be normalized
        assertEquals("test@example.com", registerRequest.getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    // --- Tests for validateAuthenticationRequest ---

    @Test
    void testValidateAuthenticationRequestShouldPassWithValidData() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@example.com", "password123");
        // Mock repository to simulate an existing user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

        // Act & Assert: No exception should be thrown
        assertDoesNotThrow(() -> userValidator.validateAuthenticationRequest(authenticationRequest));

        // Verify that the repository was checked
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testValidateAuthenticationRequestShouldThrowExceptionForNonExistentUser() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@example.com", "password123");
        // Mock repository to simulate a non-existent user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateAuthenticationRequest(authenticationRequest));

        assertEquals("User with the provided email is not registered.", exception.getMessage());
    }

    @Test
    void testValidateAuthenticationRequestShouldThrowExceptionForInvalidEmailFormat() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("invalid-email", "password123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateAuthenticationRequest(authenticationRequest));

        assertEquals("The provided email format is invalid.", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void testValidateAuthenticationRequestShouldThrowExceptionForNullEmail() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(null, "password123");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateAuthenticationRequest(authenticationRequest));

        assertEquals("The provided email format is invalid.", exception.getMessage());
    }

    @Test
    void testValidateAuthenticationRequestShouldThrowExceptionForNullPassword() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@example.com", null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateAuthenticationRequest(authenticationRequest));

        assertEquals("Password must be at least 6 characters long and contain at least one digit.", exception.getMessage());
    }

    @Test
    void testValidateAuthenticationRequestShouldThrowExceptionForInvalidPasswordFormat() {
        // Arrange
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("test@example.com", "short");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userValidator.validateAuthenticationRequest(authenticationRequest));

        assertEquals("Password must be at least 6 characters long and contain at least one digit.", exception.getMessage());
    }
}
