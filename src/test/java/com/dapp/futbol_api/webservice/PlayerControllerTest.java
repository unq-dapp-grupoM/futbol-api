package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.security.JwtAuthenticationFilter;
import com.dapp.futbol_api.security.JwtService;
import com.dapp.futbol_api.security.SimpleUserDetailsService;
import com.dapp.futbol_api.model.dto.PlayerDTO;
import com.dapp.futbol_api.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private SimpleUserDetailsService simpleUserDetailsService;

    @Test
    void testGetPlayerInfoByName_Success() throws Exception {
        // Arrange
        String playerName = "Lionel Messi";

        PlayerDTO mockPlayer = new PlayerDTO();
        mockPlayer.setName(playerName);
        mockPlayer.setCurrentTeam("Inter Miami");

        when(playerService.getPlayerInfoByName(anyString())).thenReturn(List.of(mockPlayer));

        // Act & Assert
        mockMvc.perform(get("/api/player")
                        .param("playerName", playerName))
                .andDo(print()) // Para ver qué está pasando
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(playerName))
                .andExpect(jsonPath("$[0].currentTeam").value("Inter Miami"));
    }

    @Test
    @WithMockUser
    void testGetPlayerInfoByName_PlayerNotFound() throws Exception {
        // Arrange
        String playerName = "Unknown Player";
        when(playerService.getPlayerInfoByName(playerName)).thenThrow(new IllegalArgumentException("Player not found"));

        // Act & Assert
        mockMvc.perform(get("/api/player").param("playerName", playerName))
                .andExpect(status().isBadRequest());
    }
}
