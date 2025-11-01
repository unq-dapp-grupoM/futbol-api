package com.dapp.futbol_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class SimpleUserDetailsServiceTest {

    private final SimpleUserDetailsService simpleUserDetailsService = new SimpleUserDetailsService();

    @Test
    void testLoadUserByUsernameShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";

        // Act
        UserDetails userDetails = simpleUserDetailsService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER")));
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsernameWithNullEmailShouldThrowException() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> simpleUserDetailsService.loadUserByUsername(null));
    }

    @Test
    void testLoadUserByUsernameWithEmptyEmailShouldThrowException() {
        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> simpleUserDetailsService.loadUserByUsername(""));
    }
}