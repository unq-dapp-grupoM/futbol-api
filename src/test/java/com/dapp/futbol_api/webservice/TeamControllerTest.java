package com.dapp.futbol_api.webservice;

import com.dapp.futbol_api.model.dto.TeamDTO;
import com.dapp.futbol_api.security.JwtService;
import com.dapp.futbol_api.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.dapp.futbol_api.security.SimpleUserDetailsService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SimpleUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void testGetTeamInfoByName_Success() throws Exception {
        // Arrange
        String teamName = "Real Madrid";
        TeamDTO mockTeam = new TeamDTO();
        mockTeam.setName(teamName);

        when(teamService.getTeamInfoByName(teamName)).thenReturn(mockTeam);

        // Act & Assert
        mockMvc.perform(get("/api/team").param("teamName", teamName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(teamName));
    }

    @Test
    @WithMockUser
    void testGetTeamInfoByName_TeamNotFound() throws Exception {
        // Arrange
        String teamName = "Unknown Team";
        when(teamService.getTeamInfoByName(teamName)).thenThrow(new IllegalArgumentException("Team not found"));

        // Act & Assert
        mockMvc.perform(get("/api/team").param("teamName", teamName))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetFutureMatches_Success() throws Exception {
        // Arrange
        String teamName = "Real Madrid";
        List<Object> mockMatches = new ArrayList<>();
        when(teamService.getFutureMatches(teamName)).thenReturn(mockMatches);

        // Act & Assert
        mockMvc.perform(get("/api/futureMatches").param("teamName", teamName))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetFutureMatches_TeamNotFound() throws Exception {
        // Arrange
        String teamName = "Unknown Team";
        when(teamService.getFutureMatches(teamName))
                .thenThrow(new IllegalArgumentException("Team not found for future matches."));

        // Act & Assert
        mockMvc.perform(get("/api/futureMatches").param("teamName", teamName))
                .andExpect(status().isBadRequest());
    }
}
