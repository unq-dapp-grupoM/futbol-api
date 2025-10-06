package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.PlayerDTO;
import com.dapp.futbol_api.model.dto.PlayerMatchStatsDTO;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerServiceTest {
    
    private PlayerService playerService;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    // --- Test Data ---
    private final String mockPlayerInfoHtml = "<html><body>" +
            "<div class='col12-lg-10 col12-m-10 col12-s-9 col12-xs-8'>" +
            "  <div class='col12-lg-6'><span class='info-label'>Nombre:</span> Lionel Messi</div>" +
            "  <div class='col12-lg-6'><span class='info-label'>Equipo Actual:</span> Inter Miami</div>" +
            "  <div class='col12-lg-6'><span class='info-label'>Número de Dorsal:</span> 10</div>" +
            "  <div class='col12-lg-6'><span class='info-label'>Edad:</span> 37 (24-06-1987)</div>" +
            "  <div class='col12-lg-6'><span class='info-label'>Altura:</span> 170cm</div>" +
            "  <div class='col12-lg-6'><span class='info-label'>Nacionalidad:</span> Argentina</div>" +
            "  <div><span class='info-label'>Posiciones:</span> <span><span>Delantero(Centro)</span><span>Mediapunta(Centro, Derecha)</span></span></div>" +
            "</div>" +
            "</body></html>";

    private final String mockMatchStatsHtml = "<html><body>" +
            "<table>" +
            "<tbody id='player-table-statistics-body'>" +
            "  <tr>" +
            "    <td><a class='player-match-link'>vs Equipo A <span class='scoreline'>(1-0)</span></a></td>" +
            "    <td>-</td>" + // Column 2 is ignored
            "    <td>01-Jan-24</td>" +
            "    <td>FW</td>" +
            "    <td>90</td>" +
            "    <td>1</td>" +
            "    <td>0</td>" +
            "    <td>-</td>" +
            "    <td>-</td>" +
            "    <td>5</td>" +
            "    <td>85.5</td>" +
            "    <td>2</td>" +
            "    <td>9.5</td>" +
            "  </tr>" +
            "</tbody>" +
            "</table>" +
            "</body></html>";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        playerService = new PlayerService();
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testGetPlayerInfoByNameShouldThrowExceptionWhenPlayerNotFound() {
        // Arrange: Mock the search to return no results
        context.route("**/search/**", route -> {
            route.fulfill(new Route.FulfillOptions()
                .setContentType("text/html")
                .setBody("<html><body><div class='search-result'></div></body></html>"));
        });

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.getPlayerInfoByName("Unknown Player");
        });

        assertEquals("Player with name 'Unknown Player' not found.", exception.getMessage());
    }

    @Test
    void testScrapePlayerDataShouldExtractCorrectInfo() {
        // Arrange
        page.setContent(mockPlayerInfoHtml);

        // Act
        PlayerDTO player = playerService.scrapePlayerData(page);

        // Assert
        assertNotNull(player);
        assertEquals("Lionel Messi", player.getName());
        assertEquals("Inter Miami", player.getCurrentTeam());
        assertEquals("10", player.getShirtNumber());
        assertEquals("37", player.getAge());
        assertEquals("170cm", player.getHeight());
        assertEquals("Argentina", player.getNationality());
        assertEquals("Delantero(Centro) Mediapunta(Centro, Derecha)", player.getPositions());
    }

    @Test
    void testScrapePlayerDataShouldHandleMissingInfo() {
        // Arrange: HTML with missing 'Altura'
        String incompleteHtml = "<html><body>" +
            "<div class='col12-lg-10 col12-m-10 col12-s-9 col12-xs-8'>" +
            "  <div class='col12-lg-6'><span class='info-label'>Nombre:</span> Lionel Messi</div>" +
            "</div>" +
            "</body></html>";
        page.setContent(incompleteHtml);

        // Act
        PlayerDTO player = playerService.scrapePlayerData(page);

        // Assert
        assertEquals("Lionel Messi", player.getName());
        assertEquals(AbstractWebService.NOT_FOUND, player.getHeight());
        assertEquals(AbstractWebService.NOT_FOUND, player.getPositions());
    }

    @Test
    void testScrapePlayerMatchStatsShouldExtractCorrectStats() {
        // Arrange
        page.setContent(mockMatchStatsHtml);

        // Act
        List<PlayerMatchStatsDTO> stats = playerService.scrapePlayerMatchStats(page);

        // Assert
        assertNotNull(stats);
        assertEquals(1, stats.size());

        PlayerMatchStatsDTO match = stats.get(0);
        assertEquals("vs Equipo A", match.getOpponent());
        assertEquals("(1-0)", match.getScore());
        assertEquals("01-Jan-24", match.getDate());
        assertEquals("FW", match.getPosition());
        assertEquals("90", match.getMinsPlayed());
        assertEquals("1", match.getGoals());
        assertEquals("0", match.getAssists());
        assertEquals("-", match.getYellowCards());
        assertEquals("-", match.getRedCards());
        assertEquals("5", match.getShots());
        assertEquals("85.5", match.getPassSuccess());
        assertEquals("2", match.getAerialsWon());
        assertEquals("9.5", match.getRating());
    }

    @Test
    void testScrapePlayerMatchStatsShouldReturnEmptyListForEmptyTable() {
        // Arrange
        page.setContent("<html><body><table><tbody id='player-table-statistics-body'></tbody></table></body></html>");

        // Act
        List<PlayerMatchStatsDTO> stats = playerService.scrapePlayerMatchStats(page);

        // Assert
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }
}
