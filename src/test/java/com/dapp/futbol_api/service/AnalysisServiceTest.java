package com.dapp.futbol_api.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.client.MockRestServiceServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Lionel%20Messi/performanceMetrics?userEmail=" + userEmail;
        Map<String, Object> mockMetrics = Collections.singletonMap("goals", 900);

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockMetrics), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPlayerPerformanceMetrics(playerName, authentication);

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
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Unknown%20Player/performanceMetrics?userEmail=" + userEmail;

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> analysisService.getPlayerPerformanceMetrics(playerName, authentication));
        mockServer.verify();
    }

    @Test
    void testGetPlayerMetrics_HandlesListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "List Player";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/List%20Player/performanceMetrics?userEmail=" + userEmail;
        Map<String, Object> mockMetric = Collections.singletonMap("assists", 10);
        List<Map<String, Object>> mockResponse = Collections.singletonList(mockMetric);

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPlayerPerformanceMetrics(playerName, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponse, result);
        mockServer.verify();
    }

    @Test
    void testGetPlayerMetrics_ClientError() {
        // Arrange
        String playerName = "Error Player";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Error%20Player/performanceMetrics?userEmail=" + userEmail;

        mockServer.expect(requestTo(url))
                .andRespond(withBadRequest().body("Invalid request"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.getPlayerPerformanceMetrics(playerName, authentication));
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Cristiano Ronaldo";
        String opponent = "FC Barcelona";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Cristiano%20Ronaldo/prediction?opponent=FC%20Barcelona&isHome=true&position=FW&userEmail=" + userEmail;
        Map<String, Object> mockPrediction = Collections.singletonMap("expectedGoals", 1.2);

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockPrediction), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPerformancePrediction(playerName, opponent, true, "FW", authentication);

        // Assert
        assertNotNull(result);
        assertEquals(mockPrediction, result);
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_ServiceError() {
        // Arrange
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Error%20Player/prediction?opponent=Any&isHome=false&position=GK&userEmail=" + userEmail;

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.getPerformancePrediction("Error Player", "Any", false, "GK", authentication));
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_HandlesListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "List Player";
        String opponent = "Some Team";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/List%20Player/prediction?opponent=Some%20Team&isHome=true&position=FW&userEmail=" + userEmail;
        Map<String, Object> mockPrediction = Collections.singletonMap("expectedGoals", 0.5);
        List<Map<String, Object>> mockResponse = Collections.singletonList(mockPrediction);

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPerformancePrediction(playerName, opponent, true, "FW", authentication);

        // Assert
        assertNotNull(result);
        assertEquals(mockResponse, result);
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_HandlesEmptyListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "Empty List Player";
        String opponent = "Some Team";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Empty%20List%20Player/prediction?opponent=Some%20Team&isHome=true&position=FW&userEmail=" + userEmail;
        List<Map<String, Object>> mockResponse = Collections.emptyList();

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPerformancePrediction(playerName, opponent, true, "FW", authentication);

        // Assert
        assertNull(result);
        mockServer.verify();
    }

    @Test
    void testGetPerformancePrediction_PlayerNotFound() {
        // Arrange
        String playerName = "Unknown Player";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Unknown%20Player/prediction?opponent=Any&isHome=false&position=GK&userEmail=" + userEmail;

        mockServer.expect(requestTo(url)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> analysisService.getPerformancePrediction(playerName, "Any", false, "GK", authentication));
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
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Kylian%20Mbapp%C3%A9/comparison?userEmail=" + userEmail;
        Map<String, String> mockAnalysis = Collections.singletonMap("trend", "improving");

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockAnalysis), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getComparativeAnalysis(playerName, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(mockAnalysis, result);
        mockServer.verify();
    }

    @Test
    void testGetComparativeAnalysis_HandlesEmptyListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "Empty List Player";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Empty%20List%20Player/comparison?userEmail=" + userEmail;
        List<Map<String, Object>> mockResponse = Collections.emptyList();

        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getComparativeAnalysis(playerName, authentication);

        // Assert
        assertNull(result);
        mockServer.verify();
    }

    @Test
    void testGetComparativeAnalysis_ServerError() {
        // Arrange
        String playerName = "Error Player";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/Error%20Player/comparison?userEmail=" + userEmail;

        mockServer.expect(requestTo(url))
                .andRespond(withServerError());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analysisService.getComparativeAnalysis(playerName, authentication));
        mockServer.verify();
    }

    @Test
    void testGetPlayerHistory_Success() throws JsonProcessingException {
        // Arrange
        String playerName = "Lionel Messi";
        String date = "02-11-2025";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/history/Lionel%20Messi?date=02-11-2025&userEmail=" + userEmail;

        List<Map<String, Object>> mockHistory = List.of(
                Map.of("id", 1, "queryType", "PREDICTION"),
                Map.of("id", 2, "queryType", "PERFORMANCE")
        );

        mockServer.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockHistory), MediaType.APPLICATION_JSON));

        // Act
        Object result = analysisService.getPlayerHistory(playerName, date, authentication);

        // Assert
        assertNotNull(result);
        assertEquals(mockHistory, result);
        mockServer.verify();
    }

    @Test
    void testGetPlayerHistory_PlayerNotFound() {
        // Arrange
        String playerName = "Unknown Player";
        String date = "02-11-2025";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        String url = baseUrl + "/api/analysis/history/Unknown%20Player?date=02-11-2025&userEmail=" + userEmail;

        mockServer.expect(requestTo(url))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> analysisService.getPlayerHistory(playerName, date, authentication));
        mockServer.verify();
    }

    @Test
    void testGetPlayerHistory_EmptyListResponse() throws JsonProcessingException {
        // Arrange
        String playerName = "NoHistory Player";
        String date = "02-11-2025";
        String userEmail = "test@example.com";
        UserDetails userDetails = new User(userEmail, "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        String url = baseUrl + "/api/analysis/history/NoHistory%20Player?date=02-11-2025&userEmail=" + userEmail;
        mockServer.expect(requestTo(url))
                .andRespond(withSuccess(objectMapper.writeValueAsString(Collections.emptyList()), MediaType.APPLICATION_JSON));
        // Act
        Object result = analysisService.getPlayerHistory(playerName, date, authentication);
        // Assert
        assertNull(result);
        mockServer.verify();
    }

    // Note: Testing the catch block for UnsupportedEncodingException in decodeUrlParameter
    // is impractical because StandardCharsets.UTF_8.name() is guaranteed to be a valid
    // encoding name in any standard Java environment. A test for this would require
    // manipulating the JVM's supported charsets, which is beyond the scope of a
    // standard unit test. The happy path is implicitly tested by all other tests.
}