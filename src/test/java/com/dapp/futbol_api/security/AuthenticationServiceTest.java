package com.dapp.futbol_api.security;

import com.dapp.futbol_api.model.Role;
import com.dapp.futbol_api.model.User;
import com.dapp.futbol_api.repositories.UserRepository;
import com.dapp.futbol_api.utils.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void testRegisterShouldSucceedWithValidRequest() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        String encodedPassword = "encodedPassword";

        // Mock the dependencies' behavior
        doNothing().when(userValidator).validateRegistrationRequest(request);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        // Act
        String result = authenticationService.register(request);

        // Assert
        assertEquals("User registered successfully", result);

        // Verify that the validator was called
        verify(userValidator).validateRegistrationRequest(request);

        // Capture the user object passed to the repository
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Assert properties of the saved user
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
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
        User user = User.builder().email(request.getEmail()).password("encodedPassword").role(Role.USER).build();
        String expectedToken = "dummy-jwt-token";

        // Mock dependencies
        doNothing().when(userValidator).validateAuthenticationRequest(request);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(expectedToken);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());

        // Verify interactions
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        verify(jwtService).generateToken(user);
    }

    @Test
    void testAuthenticateShouldThrowExceptionWithInvalidCredentials() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "wrong-password");
        doNothing().when(userValidator).validateAuthenticationRequest(request);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));

        // Verify that a token is never generated
        verify(jwtService, never()).generateToken(any(User.class));
    }

}
