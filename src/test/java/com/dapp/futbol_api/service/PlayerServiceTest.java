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

@RestClientTest(PlayerService.class)
public class PlayerServiceTest {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseUrl = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        mockServer.reset();
    }

    @Test
    void testGetPlayerInfoByName_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Lionel Messi";
        String url = baseUrl + "/api/scrape/player?playerName=Lionel%20Messi";
        Map<String, Object> mockPlayer = Map.of("name", "Lionel Messi", "team", "Inter Miami");
        List<Map<String, Object>> mockResponse = Collections.singletonList(mockPlayer);

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = playerService.getPlayerInfoByName(playerName);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals("Lionel Messi", ((Map<?, ?>) result).get("name"));
        mockServer.verify();
    }

    @Test
    void testGetPlayerInfoByName_NotFound_HttpStatus() {
        // Arrange
        String playerName = "Ghost Player";
        String url = baseUrl + "/api/scrape/player?playerName=Ghost%20Player";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> playerService.getPlayerInfoByName(playerName));
        mockServer.verify();
    }

    @Test
    void testGetPlayerInfoByName_ServerError() {
        // Arrange
        String playerName = "Error Player";
        String url = baseUrl + "/api/scrape/player?playerName=Error%20Player";

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> playerService.getPlayerInfoByName(playerName));
        mockServer.verify();
    }
}