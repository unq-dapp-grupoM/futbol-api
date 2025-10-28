package com.dapp.futbol_api.service;

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
}