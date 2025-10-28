package com.dapp.futbol_api.config;

import com.dapp.futbol_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfig.class)
// Provide a dummy value for the api key property required by ApiKeyAuthFilter
@TestPropertySource(properties = "api.security.key=test-api-key")
public class SecurityConfigTest {
    

    @Autowired
    private MockMvc mockMvc;

    // This bean is part of Spring Security's auto-configuration but is not loaded by default in a focused @WebMvcTest.
    @MockitoBean
    private UserDetailsService userDetailsService;

    // Mock the dependency of JwtAuthenticationFilter
    @MockitoBean
    private JwtService jwtService;

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/api/auth/login",
            "/v3/api-docs/swagger-config",
            "/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config",
            "/swagger-ui.html",
            "/actuator/health"
    })
    void testWhiteListedUrlsShouldBePermitted(String url) throws Exception {
        // Act & Assert
        // These requests should pass security and result in either 200 OK, 3xx Redirection, or 404 Not Found,
        // but never 401 Unauthorized or 403 Forbidden.
        mockMvc.perform(get(url))
                .andExpect(status().is(org.hamcrest.Matchers.not(org.hamcrest.Matchers.is(401))))
                .andExpect(status().is(org.hamcrest.Matchers.not(org.hamcrest.Matchers.is(403))));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/player?playerName=messi",
            "/api/team?teamName=barcelona",
            "/api/analysis/messi/metrics",
            "/api/futureMatches?teamName=barcelona"
    })
    void testUserUrlsShouldBeForbiddenWithoutAuthentication(String url) throws Exception {
        // Act & Assert
        // These requests should be forbidden as they require USER authority.
        mockMvc.perform(get(url))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAnyOtherRequestShouldBeUnauthorizedWithoutAuthentication() throws Exception {
        // Arrange
        String protectedUrl = "/api/some-protected-endpoint";

        // Act & Assert
        mockMvc.perform(get(protectedUrl))
                .andExpect(status().isForbidden());
    }

    @Test
    void testInternalApiShouldBeUnauthorizedWithoutApiKey() throws Exception {
        // Arrange
        String internalUrl = "/api/v1/internal/status";

        // Act & Assert
        mockMvc.perform(get(internalUrl))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser // Simulates a logged-in user with default role 'USER'
    void testAnyOtherRequestShouldBeAccessibleWithAuthentication() throws Exception {
        // Arrange
        String protectedUrl = "/api/some-protected-endpoint";

        // Act & Assert
        // The request should pass security. A 404 is expected if no controller handles the endpoint.
        mockMvc.perform(get(protectedUrl))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SERVICE") // A user with the required 'SERVICE' role
    void testInternalApiShouldBeAccessibleForServiceRole() throws Exception {
        // Arrange
        String internalUrl = "/api/v1/internal/status";

        // Act & Assert
        // The request should pass security. A 404 is expected if no controller handles the endpoint.
        mockMvc.perform(get(internalUrl))
                .andExpect(status().isNotFound());
    }
}
