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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SimpleUserDetailsService simpleUserDetailsService; // Este SÍ se usa

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternalShouldSetAuthenticationForValidToken() throws ServletException, IOException {
        // Arrange
        String userEmail = "test@example.com";
        String jwt = "valid-jwt";
        String authHeader = "Bearer " + jwt;
        UserDetails userDetails = new User(userEmail, "password", new ArrayList<>());

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        
        // CORREGIDO: Usar simpleUserDetailsService en lugar de userDetailsService
        when(simpleUserDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        
        // Usar any() para evitar problemas de matching estricto
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    // Actualizar TODOS los tests para usar simpleUserDetailsService
    @Test
    void testDoFilterInternalShouldNotAuthenticateWhenHeaderIsMissing() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, simpleUserDetailsService); // Cambiado
    }

    @Test
    void testDoFilterInternalShouldNotAuthenticateWhenHeaderIsInvalid() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, simpleUserDetailsService); // Cambiado
    }

    @Test
    void testDoFilterInternalShouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        // Arrange
        String jwt = "invalid-jwt";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtService.extractUsername(jwt)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalShouldSkipWhenAuthenticationAlreadyExists() throws ServletException, IOException {
        // Arrange
        String jwt = "valid-jwt";
        String authHeader = "Bearer " + jwt;

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("already-authenticated-user", null));

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn("test@example.com");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertEquals("already-authenticated-user", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(simpleUserDetailsService, never()).loadUserByUsername(anyString()); // Cambiado
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalShouldSkipWhenUsernameIsNull() throws ServletException, IOException {
        // Arrange
        String jwt = "jwt-with-null-user";
        String authHeader = "Bearer " + jwt;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternalShouldNotAuthenticateWhenTokenValidationFails() throws ServletException, IOException {
        // Arrange
        String userEmail = "test@example.com";
        String jwt = "valid-token-format-but-invalid";
        String authHeader = "Bearer " + jwt;
        UserDetails userDetails = new User(userEmail, "password", new ArrayList<>());

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(simpleUserDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails); // Cambiado
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(false); // Usar any()

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}