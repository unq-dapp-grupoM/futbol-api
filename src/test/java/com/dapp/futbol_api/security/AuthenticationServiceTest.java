package com.dapp.futbol_api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;

import com.dapp.futbol_api.model.User;
import com.dapp.futbol_api.repositories.UserRepository;
import com.dapp.futbol_api.service.ScraperUserService;
import com.dapp.futbol_api.utils.UserValidator;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserValidator userValidator;
    @Mock
    private ScraperUserService scraperUserService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void testRegisterShouldSucceedWithValidRequest() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        String expectedMessage = "User registered in scraper service";

        // Mock the dependencies' behavior
        doNothing().when(userValidator).validateRegistrationRequest(request);
        when(scraperUserService.registerUser(request)).thenReturn(expectedMessage);

        // Act
        String result = authenticationService.register(request);

        // Assert - Usa el mensaje real que retorna ScraperUserService
        assertEquals(expectedMessage, result);

        // Verify interactions
        verify(userValidator).validateRegistrationRequest(request);
        verify(scraperUserService).registerUser(request);

        // No verifiques userRepository o passwordEncoder ya que ScraperUserService se encarga
    }

    @Test
    void testRegisterShouldThrowExceptionWhenValidationFails() {
        // Arrange
        RegisterRequest request = new RegisterRequest("invalid-email", "pass");
        doThrow(new IllegalArgumentException("Invalid email")).when(userValidator).validateRegistrationRequest(request);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(request));

        // Verify that no user was saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testAuthenticateShouldReturnTokenWithValidCredentials() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password123");
        String expectedToken = "dummy-jwt-token";

        // Mock dependencies
        doNothing().when(userValidator).validateAuthenticationRequest(request);

        // MOCK CORRECTO: scraperUserService.validateUser debe retornar true
        when(scraperUserService.validateUser(request.getEmail(), request.getPassword()))
                .thenReturn(true);

        // Mock JWT service - usa any(UserDetails.class) ya que creas un UserDetails temporal
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(expectedToken);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());

        // Verify interactions CORRECTAS
        verify(userValidator).validateAuthenticationRequest(request);
        verify(scraperUserService).validateUser(request.getEmail(), request.getPassword());
        verify(jwtService).generateToken(any(UserDetails.class));

        // NO debería llamar a authenticationManager
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testAuthenticateShouldThrowExceptionWithInvalidCredentials() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "wrong-password");

        // Mock dependencies
        doNothing().when(userValidator).validateAuthenticationRequest(request);

        // MOCK: scraperUserService.validateUser retorna false
        when(scraperUserService.validateUser(request.getEmail(), request.getPassword()))
                .thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(request));

        // Verify interactions
        verify(userValidator).validateAuthenticationRequest(request);
        verify(scraperUserService).validateUser(request.getEmail(), request.getPassword());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

}
