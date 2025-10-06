package com.dapp.futbol_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApiKeyAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthFilter apiKeyAuthFilter;

    @AfterEach
    void tearDown() {
        // Clear the security context after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternalShouldSetAuthenticationForValidApiKey() throws ServletException, IOException {
        // Arrange
        String validApiKey = "my-secret-api-key";
        ReflectionTestUtils.setField(apiKeyAuthFilter, "principalRequestHeader", validApiKey);
        when(request.getHeader("X-API-KEY")).thenReturn(validApiKey);

        // Act
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("api-service", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SERVICE")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalShouldNotAuthenticateForInvalidApiKey() throws ServletException, IOException {
        // Arrange
        String validApiKey = "my-secret-api-key";
        ReflectionTestUtils.setField(apiKeyAuthFilter, "principalRequestHeader", validApiKey);
        when(request.getHeader("X-API-KEY")).thenReturn("wrong-key");

        // Act
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalShouldNotAuthenticateWhenApiKeyIsMissing() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("X-API-KEY")).thenReturn(null);

        // Act
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalShouldSkipWhenAuthenticationAlreadyExists() throws ServletException, IOException {
        // Arrange: Simulate an existing authentication
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("existing-user", null));

        // Act
        apiKeyAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert: Verify that the filter did not try to read the header and just continued
        verify(request, never()).getHeader("X-API-KEY");
        verify(filterChain).doFilter(request, response);
    }
}
