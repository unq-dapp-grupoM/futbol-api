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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(AnalysisService.class)
class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

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
    void testGetPlayerMetrics_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Lionel Messi";
        String url = baseUrl + "/api/analysis/Lionel%20Messi/performanceMetrics";
        Map<String, Object> mockMetrics = Collections.singletonMap("goals", 900);

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockMetrics), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPlayerPerformanceMetrics(playerName);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(900, ((Map<?, ?>) result).get("goals"));
        mockServer.verify();
    }

    @Test
    void testGetPlayerMetrics_PlayerNotFound() {
        // Arrange
        String playerName = "Unknown Player";
        String url = baseUrl + "/api/analysis/Unknown%20Player/performanceMetrics";

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> analysisService.getPlayerPerformanceMetrics(playerName));
        mockServer.verify();
    }

    @Test
    void testGetPlayerMetrics_HandlesListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "List Player";
        String url = baseUrl + "/api/analysis/List%20Player/performanceMetrics";
        Map<String, Object> mockMetric = Collections.singletonMap("assists", 10);
        List<Map<String, Object>> mockResponse = Collections.singletonList(mockMetric);

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPlayerPerformanceMetrics(playerName);

        // Assert
        assertNotNull(result);
        assertEquals(mockMetric, result);
        mockServer.verify();
    }

    @Test
    void testGetPlayerMetrics_ClientError() {
        // Arrange
        String playerName = "Error Player";
        String url = baseUrl + "/api/analysis/Error%20Player/performanceMetrics";

        mockServer.expect(requestTo(url))
                .andRespond(withBadRequest().body("Invalid request"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.getPlayerPerformanceMetrics(playerName));
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Cristiano Ronaldo";
        String opponent = "FC Barcelona";
        String url = baseUrl + "/api/analysis/Cristiano%20Ronaldo/prediction?opponent=FC%20Barcelona&isHome=true&position=FW";
        Map<String, Object> mockPrediction = Collections.singletonMap("expectedGoals", 1.2);

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockPrediction), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPerformancePrediction(playerName, opponent, true, "FW");

        // Assert
        assertNotNull(result);
        assertEquals(mockPrediction, result);
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_ServiceError() {
        // Arrange
        String url = baseUrl + "/api/analysis/Error%20Player/prediction?opponent=Any&isHome=false&position=GK";

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.getPerformancePrediction("Error Player", "Any", false, "GK"));
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_HandlesListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "List Player";
        String opponent = "Some Team";
        String url = baseUrl + "/api/analysis/List%20Player/prediction?opponent=Some%20Team&isHome=true&position=FW";
        Map<String, Object> mockPrediction = Collections.singletonMap("expectedGoals", 0.5);
        List<Map<String, Object>> mockResponse = Collections.singletonList(mockPrediction);

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPerformancePrediction(playerName, opponent, true, "FW");

        // Assert
        assertNotNull(result);
        assertEquals(mockPrediction, result);
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_HandlesEmptyListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "Empty List Player";
        String opponent = "Some Team";
        String url = baseUrl + "/api/analysis/Empty%20List%20Player/prediction?opponent=Some%20Team&isHome=true&position=FW";
        List<Map<String, Object>> mockResponse = Collections.emptyList();

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPerformancePrediction(playerName, opponent, true, "FW");

        // Assert
        assertNull(result);
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_PlayerNotFound() {
        // Arrange
        String playerName = "Unknown Player";
        String url = baseUrl + "/api/analysis/Unknown%20Player/prediction?opponent=Any&isHome=false&position=GK";

        mockServer.expect(requestTo(url)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> analysisService.getPerformancePrediction(playerName, "Any", false, "GK"));
        mockServer.verify();
    }

    @Test
    void testConvertPlayerData_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Neymar Jr";
        String url = baseUrl + "/api/analysis/Neymar%20Jr/convert-data";
        Map<String, String> mockResponse = Collections.singletonMap("status", "completed");

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.convertPlayerData(playerName);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponse, result);
        mockServer.verify();
    }

    @Test
    void testConvertPlayerData_NotFound() {
        // Arrange
        String playerName = "Ghost Player";
        String url = baseUrl + "/api/analysis/Ghost%20Player/convert-data";

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> analysisService.convertPlayerData(playerName));
        mockServer.verify();
    }

    @Test
    void testConvertPlayerData_ServerError() {
        // Arrange
        String playerName = "Server Error Player";
        String url = baseUrl + "/api/analysis/Server%20Error%20Player/convert-data";

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.convertPlayerData(playerName));
        mockServer.verify();
    }

    @Test
    void testGetComparativeAnalysis_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Kylian Mbappé";
        String url = baseUrl + "/api/analysis/Kylian%20Mbapp%C3%A9/comparison";
        Map<String, String> mockAnalysis = Collections.singletonMap("trend", "improving");

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockAnalysis), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getComparativeAnalysis(playerName);

        // Assert
        assertNotNull(result);
        assertEquals(mockAnalysis, result);
        mockServer.verify();
    }

    @Test
    void testGetComparativeAnalysis_HandlesEmptyListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "Empty List Player";
        String url = baseUrl + "/api/analysis/Empty%20List%20Player/comparison";
        List<Map<String, Object>> mockResponse = Collections.emptyList();

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getComparativeAnalysis(playerName);

        // Assert
        assertNull(result);
        mockServer.verify();
    }

    @Test
    void testGetComparativeAnalysis_ServerError() {
        // Arrange
        String playerName = "Error Player";
        String url = baseUrl + "/api/analysis/Error%20Player/comparison";

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.getComparativeAnalysis(playerName));
        mockServer.verify();
    }

    // Note: Testing the catch block for UnsupportedEncodingException in decodeUrlParameter
    // is impractical because StandardCharsets.UTF_8.name() is guaranteed to be a valid
    // encoding name in any standard Java environment. A test for this would require
    // manipulating the JVM's supported charsets, which is beyond the scope of a
    // standard unit test. The happy path is implicitly tested by all other tests.
}