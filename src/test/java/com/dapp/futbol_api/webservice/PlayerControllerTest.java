package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.security.JwtService;
import com.dapp.futbol_api.model.dto.PlayerDTO;
import com.dapp.futbol_api.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void testGetPlayerInfoByName_Success() throws Exception {
        // Arrange
        String playerName = "Lionel Messi";
        PlayerDTO mockPlayer = new PlayerDTO();
        mockPlayer.setName(playerName);
        mockPlayer.setCurrentTeam("Inter Miami");

        when(playerService.getPlayerInfoByName(playerName)).thenReturn(mockPlayer);

        // Act & Assert
        mockMvc.perform(get("/api/searchPlayer/playerName").param("playerName", playerName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(playerName))
                .andExpect(jsonPath("$.currentTeam").value("Inter Miami"));
    }

    @Test
    @WithMockUser
    void testGetPlayerInfoByName_PlayerNotFound() throws Exception {
        // Arrange
        String playerName = "Unknown Player";
        when(playerService.getPlayerInfoByName(playerName)).thenThrow(new IllegalArgumentException("Player not found"));

        // Act & Assert
        mockMvc.perform(get("/api/searchPlayer/playerName").param("playerName", playerName))
                .andExpect(status().isBadRequest());
    }
}
