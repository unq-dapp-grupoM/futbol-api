package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.TeamPlayerDTO;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TeamServiceTest {

    private TeamService teamService;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    // --- Test Data ---
    private final String mockSquadHtml = "<html><body>" +
            "<table>" +
            "<tbody id='player-table-statistics-body'>" +
            "  <tr>" +
            "    <td><a class='player-link'><span class='iconize-icon-left'>Player One</span></a><span class='player-meta-data'>25</span><span class='player-meta-data'>, Midfielder</span></td>" +
            "    <td>-</td>" + // Ignored
            "    <td>180cm</td>" +
            "    <td>75kg</td>" +
            "    <td>10 (2)</td>" +
            "    <td>980</td>" +
            "    <td>5</td>" +
            "    <td>3</td>" +
            "    <td>1</td>" +
            "    <td>0</td>" +
            "    <td>2.5</td>" +
            "    <td>88%</td>" +
            "    <td>1.5</td>" +
            "    <td>2</td>" +
            "    <td>7.8</td>" +
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
        teamService = new TeamService();
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testGetTeamInfoByNameShouldThrowExceptionWhenTeamNotFound() {
        // Arrange: Mock the search to return no results
        context.route("**/search/**", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setContentType("text/html")
                    .setBody("<html><body><div class='search-result'></div></body></html>"));
        });

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teamService.getTeamInfoByName("Unknown Team");
        });

        assertTrue(exception.getMessage().contains("Failed to fetch data from whoscored.com"));
    }

    @Test
    void testScrapeSquadDataShouldExtractCorrectInfo() {
        // Arrange
        page.setContent(mockSquadHtml);

        // Act
        List<TeamPlayerDTO> squad = teamService.scrapeSquadData(page);

        // Assert
        assertNotNull(squad);
        assertEquals(1, squad.size());

        TeamPlayerDTO player = squad.get(0);
        assertEquals("Player One", player.getName());
        assertEquals("25", player.getAge());
        assertEquals("Midfielder", player.getPosition());
        assertEquals("180cm", player.getHeight());
        assertEquals("75kg", player.getWeight());
        assertEquals("7.8", player.getRating());
    }

    @Test
    void testScrapeSquadDataShouldReturnEmptyListForEmptyTable() {
        // Arrange
        page.setContent("<html><body><table><tbody id='player-table-statistics-body'></tbody></table></body></html>");

        // Act
        List<TeamPlayerDTO> squad = teamService.scrapeSquadData(page);

        // Assert
        assertNotNull(squad);
        assertTrue(squad.isEmpty());
    }
}