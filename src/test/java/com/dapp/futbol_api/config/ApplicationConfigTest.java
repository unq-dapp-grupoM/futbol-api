package com.dapp.futbol_api.config;

import com.dapp.futbol_api.model.User;
import com.dapp.futbol_api.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationConfig applicationConfig;

    @Test
    void testUserDetailsServiceShouldLoadUserSuccessfully() {
        // Arrange
        String userEmail = "test@example.com";
        User user = User.builder().email(userEmail).password("password").build();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // Act
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Assert
        assertNotNull(userDetails);
        assertEquals(userEmail, userDetails.getUsername());
    }

    @Test
    void testUserDetailsServiceShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String userEmail = "notfound@example.com";
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act
        UserDetailsService userDetailsService = applicationConfig.userDetailsService();

        // Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(userEmail));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testAuthenticationManagerShouldBeRetrievedFromConfig() throws Exception {
        // Arrange
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        // Act
        AuthenticationManager result = applicationConfig.authenticationManager(authConfig);

        // Assert
        assertNotNull(result);
        assertEquals(authManager, result);
    }

    @Test
    void testPasswordEncoderShouldReturnBCryptPasswordEncoder() {
        // Arrange & Act
        PasswordEncoder passwordEncoder = applicationConfig.passwordEncoder();

        // Assert
        assertNotNull(passwordEncoder);
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);
    }
}
