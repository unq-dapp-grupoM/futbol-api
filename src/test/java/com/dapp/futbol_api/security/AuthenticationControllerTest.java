package com.dapp.futbol_api.security;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dapp.futbol_api.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class) // Explicitly import your security configuration
@TestPropertySource(properties = "api.security.key=test-api-key") // Provide dummy property for ApiKeyAuthFilter
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SimpleUserDetailsService userDetailsService;

    @Test
    void testRegisterShouldReturnOkOnSuccess() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        String successMessage = "User registered successfully!";
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(successMessage);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }

    @Test
    void testRegisterShouldReturnBadRequestOnServiceError() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        String errorMessage = "Email already in use.";
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void testAuthenticateShouldReturnOkAndTokenOnSuccess() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password123");
        AuthenticationResponse response = new AuthenticationResponse("dummy-jwt-token");
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy-jwt-token"));
    }

    @Test
    void testAuthenticateShouldReturnBadRequestOnBadCredentials() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "wrong-password");
        String errorMessage = "Invalid credentials";
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException(errorMessage));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(errorMessage));
    }
}