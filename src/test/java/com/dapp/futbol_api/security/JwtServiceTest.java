package com.dapp.futbol_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private final String testSecretKey = "bXlzdXBlcnNlY3JldGtleWZvcnRlc3RpbmdwdXJwb3Nlc2FuZGl0c2hvdWxkYmVsb25nZW5vdWdo"; // A valid Base64 key

    @Test
    void testExtractUsernameShouldReturnCorrectUsername() {
        // Arrange
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour

        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    void testGenerateTokenWithExtraClaims() {
        // Arrange
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        String customClaimValue = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));
        assertEquals("customValue", customClaimValue);
    }

    @Test
    void testIsTokenValidShouldReturnTrueForValidToken() {
        // Arrange
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValidShouldReturnFalseForDifferentUser() {
        // Arrange
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        UserDetails originalUser = new User("user1@example.com", "password", new ArrayList<>());
        UserDetails differentUser = new User("user2@example.com", "password", new ArrayList<>());
        String token = jwtService.generateToken(originalUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValidShouldReturnFalseForExpiredToken() {
        // Arrange
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L); // Expired 1 second ago

        UserDetails userDetails = new User("test@example.com", "password", new ArrayList<>());
        String expiredToken = jwtService.generateToken(userDetails);

        // Act & Assert
        assertFalse(jwtService.isTokenValid(expiredToken, userDetails));
    }
}
