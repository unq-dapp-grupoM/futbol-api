package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.config.SecurityConfig;
import com.dapp.futbol_api.security.JwtService;
import com.dapp.futbol_api.security.SimpleUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RootController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "api.security.key=test-api-key")
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock dependencies required by SecurityConfig
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SimpleUserDetailsService userDetailsService;

    @Test
    void testHomeEndpointIsPublicAndReturnsCorrectMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Football API running correctly"));
    }
}