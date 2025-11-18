package com.dapp.futbol_api.service;

import com.dapp.futbol_api.exception.TeamServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(TeamService.class)
class TeamServiceTest {

    @Autowired
    private TeamService teamService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseUrl = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }

    // --- Tests for getTeamInfoByName ---

    @Test
    void testGetTeamInfoByName_Success() throws JsonProcessingException {
        // Arrange
        String teamName = "Real Madrid";
        String url = baseUrl + "/api/scrape/team?teamName=Real%20Madrid";
        Map<String, Object> mockTeam = Map.of("name", "Real Madrid", "stadium", "Santiago Bernabeu");
        List<Map<String, Object>> mockResponse = Collections.singletonList(mockTeam);

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = teamService.getTeamInfoByName(teamName);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals("Real Madrid", ((Map<?, ?>) result).get("name"));
        mockServer.verify();
    }

    @Test
    void testGetTeamInfoByName_NotFound_EmptyList() throws JsonProcessingException {
        // Arrange
        String teamName = "Unknown Team";
        String url = baseUrl + "/api/scrape/team?teamName=Unknown%20Team";
        List<Map<String, Object>> mockResponse = Collections.emptyList();

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> teamService.getTeamInfoByName(teamName));
        mockServer.verify();
    }

    @Test
    void testGetTeamInfoByName_NotFound_HttpStatus() {
        // Arrange
        String teamName = "Ghost Team";
        String url = baseUrl + "/api/scrape/team?teamName=Ghost%20Team";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> teamService.getTeamInfoByName(teamName));
        mockServer.verify();
    }

    @Test
    void testGetTeamInfoByName_ServerError() {
        // Arrange
        String teamName = "Error Team";
        String url = baseUrl + "/api/scrape/team?teamName=Error%20Team";

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> teamService.getTeamInfoByName(teamName));
        mockServer.verify();
    }

    // --- Tests for getFutureMatches ---

    @Test
    void testGetFutureMatches_Success() throws JsonProcessingException {
        // Arrange
        String teamName = "FC Barcelona";
        String url = baseUrl + "/api/scrape/futureMatches?teamName=FC%20Barcelona";
        List<Map<String, String>> mockMatches = Collections.singletonList(Map.of("opponent", "Real Madrid"));

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockMatches), MediaType.APPLICATION_JSON));

        // Act
        Object result = teamService.getFutureMatches(teamName);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertFalse(((List<?>) result).isEmpty());
        mockServer.verify();
    }

    @Test
    void testGetFutureMatches_NotFound() {
        // Arrange
        String teamName = "NonExistent Team";
        String url = baseUrl + "/api/scrape/futureMatches?teamName=NonExistent%20Team";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> teamService.getFutureMatches(teamName));
        mockServer.verify();
    }

    // --- Tests for compareTeams ---

    @Test
    void testCompareTeams_Success() throws JsonProcessingException {
        // Arrange
        String team1 = "Barcelona";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Barcelona&team2=Real%20Madrid";

        Map<String, Object> mockComparison = Map.of(
                "teamName1", "Barcelona",
                "teamName2", "Real Madrid",
                "comparison", Map.of(
                        "team1OverallRating", 7.8,
                        "team2OverallRating", 7.5,
                        "prediction", Map.of(
                                "team1Wins", 45,
                                "team2Wins", 35,
                                "draws", 20,
                                "favoredTeam", "Barcelona"
                        )
                ),
                "suggestedWinner", "SLIGHT_ADVANTAGE_Barcelona (7.8 rating)",
                "confidenceLevel", 0.75
        );

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockComparison), MediaType.APPLICATION_JSON));

        // Act
        Object result = teamService.compareTeams(team1, team2);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);

        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertEquals("Barcelona", resultMap.get("teamName1"));
        assertEquals("Real Madrid", resultMap.get("teamName2"));
        assertNotNull(resultMap.get("comparison"));
        assertNotNull(resultMap.get("suggestedWinner"));
        assertNotNull(resultMap.get("confidenceLevel"));

        mockServer.verify();
    }

    @Test
    void testCompareTeams_Team1NotFound() {
        // Arrange
        String team1 = "Unknown Team";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Unknown%20Team&team2=Real%20Madrid";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> teamService.compareTeams(team1, team2)
        );

        assertTrue(exception.getMessage().contains("One or both teams not found"));
        mockServer.verify();
    }

    @Test
    void testCompareTeams_Team2NotFound() {
        // Arrange
        String team1 = "Barcelona";
        String team2 = "Ghost Team";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Barcelona&team2=Ghost%20Team";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> teamService.compareTeams(team1, team2)
        );

        assertTrue(exception.getMessage().contains("One or both teams not found"));
        mockServer.verify();
    }

    @Test
    void testCompareTeams_BothTeamsNotFound() {
        // Arrange
        String team1 = "Unknown Team 1";
        String team2 = "Unknown Team 2";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Unknown%20Team%201&team2=Unknown%20Team%202";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> teamService.compareTeams(team1, team2)
        );

        assertTrue(exception.getMessage().contains("One or both teams not found"));
        mockServer.verify();
    }

    @Test
    void testCompareTeams_NullResponse() throws JsonProcessingException {
        // Arrange
        String team1 = "Barcelona";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Barcelona&team2=Real%20Madrid";

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        // Act & Assert
        TeamServiceException exception = assertThrows(
                TeamServiceException.class,
                () -> teamService.compareTeams(team1, team2)
        );

        assertTrue(exception.getMessage().contains("Error comparing teams."));
        mockServer.verify();
    }

    @Test
    void testCompareTeams_ServerError() {
        // Arrange
        String team1 = "Barcelona";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Barcelona&team2=Real%20Madrid";

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        TeamServiceException exception = assertThrows(
                TeamServiceException.class,
                () -> teamService.compareTeams(team1, team2)
        );

        assertEquals("Error comparing teams.", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void testCompareTeams_ConnectionError() {
        // Arrange
        String team1 = "Barcelona";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Barcelona&team2=Real%20Madrid";

        mockServer.expect(requestTo(url))
                .andRespond(withBadRequest()); // Simulate any client error

        // Act & Assert
        TeamServiceException exception = assertThrows(
                TeamServiceException.class,
                () -> teamService.compareTeams(team1, team2)
        );

        assertEquals("Error comparing teams.", exception.getMessage());
        mockServer.verify();
    }

    @Test
    void testCompareTeams_WithSpecialCharacters() throws JsonProcessingException {
        // Arrange
        String team1 = "Atlético Madrid";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=Atlético%20Madrid&team2=Real%20Madrid";

        Map<String, Object> mockComparison = Map.of(
                "teamName1", "Atlético Madrid",
                "teamName2", "Real Madrid",
                "comparison", Map.of("team1OverallRating", 7.6, "team2OverallRating", 7.8)
        );

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockComparison), MediaType.APPLICATION_JSON));

        // Act
        Object result = teamService.compareTeams(team1, team2);

        // Assert
        assertNotNull(result);
        mockServer.verify();
    }

    @Test
    void testCompareTeams_EmptyTeams() {
        // Arrange
        String team1 = "";
        String team2 = "Real Madrid";
        String url = baseUrl + "/api/scrape/teams/compare?team1=&team2=Real%20Madrid";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> teamService.compareTeams(team1, team2));
        mockServer.verify();
    }


}