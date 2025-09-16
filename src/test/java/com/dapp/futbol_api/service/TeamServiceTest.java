package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.GameMatchDTO;
import com.dapp.futbol_api.model.dto.TeamDTO;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamServiceTest {

    private TeamService teamService;

    @BeforeAll
    static void setUpDriver() {
        // Si chromedriver no está en el PATH del sistema, descomenta y ajusta la
        // siguiente línea:
        // System.setProperty("webdriver.chrome.driver",
        // "c:/path/to/your/chromedriver.exe");
    }

    @Test
    @Tag("unit")
    void getTeamInfoFromLocalFile_shouldParseTeamAndFixtures() throws MalformedURLException {
        // Arrange
        teamService = new TeamService();
        String teamName = "Boca Juniors";

        // --- Este bloque carga el HTML local en lugar de navegar por internet ---
        // El archivo ahora se carga desde la carpeta de recursos de test.
        Path resourcePath = Paths.get("src", "test", "resources", "team_boca_juniors.html");
        URL resourceUrl = resourcePath.toUri().toURL();
        ChromeOptions options = new ChromeOptions();
        // --- Opciones para un modo headless más robusto ---
        // El nuevo modo headless es más fiable y se comporta como un navegador normal.
        options.addArguments("--headless=new");
        // Definir un tamaño de ventana previene problemas con diseños web responsivos.
        options.addArguments("--window-size=1920,1080");
        // Opciones adicionales que solucionan problemas comunes en diferentes entornos.
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // ¡ESTA ES LA LÍNEA CLAVE! Desactiva la ejecución de JavaScript.
        options.addArguments("--disable-javascript");
        WebDriver driver = new ChromeDriver(options);
        // --------------------------------------------------------------------

        try {
            // Act
            driver.get(resourceUrl.toString());
            // Llamamos directamente al método de parsing con el driver que contiene el HTML
            // local
            TeamDTO team = teamService.scrapeTeamData(driver);

            // Assert
            assertNotNull(team, "El DTO del equipo no debería ser nulo.");
            // Nota: El nombre del equipo se extrae de la página, así que podría variar.
            // Ajusta la aserción según el contenido de tu HTML guardado.
            assertEquals("Boca Juniors", team.getName());
            assertNotNull(team.getFixture(), "La lista de partidos no debería ser nula.");
            assertFalse(team.getFixture().isEmpty(), "La lista de partidos no debería estar vacía.");
        } finally {
            driver.quit();
        }
    }

    @Test
    @Tag("integration")
    @Disabled("Test de integración, lento y requiere internet.")
    void getTeamInfoByName_shouldParseTeamAndFixtures() {
        // Arrange
        teamService = new TeamService();
        String teamName = "Boca Juniors";

        // Act
        TeamDTO team = teamService.getTeamInfoByName(teamName);

        // Assert
        assertNotNull(team, "El DTO del equipo no debería ser nulo.");
        assertEquals(teamName, team.getName());

        assertNotNull(team.getFixture(), "La lista de partidos no debería ser nula.");
        assertFalse(team.getFixture().isEmpty(), "La lista de partidos no debería estar vacía.");

        // Verificamos que los datos de al menos un partido tengan sentido
        GameMatchDTO firstMatch = team.getFixture().get(0);
        assertNotNull(firstMatch.getCup(), "La copa no puede ser nula.");
        assertFalse(firstMatch.getCup().isEmpty(), "La copa no puede estar vacía.");

        assertNotNull(firstMatch.getDate(), "La fecha no puede ser nula.");
        assertNotEquals("No encontrado", firstMatch.getDate());

        assertNotNull(firstMatch.getResult(), "El resultado no puede ser nulo.");
        assertFalse(firstMatch.getResult().isEmpty(), "El resultado no puede estar vacío.");

        assertNotNull(firstMatch.getActualTeam(), "El equipo actual no puede ser nulo.");
        assertEquals(teamName, firstMatch.getActualTeam());

        assertNotNull(firstMatch.getRivalTeam(), "El equipo rival no puede ser nulo.");
        assertFalse(firstMatch.getRivalTeam().isEmpty(), "El equipo rival no puede estar vacío.");
    }

    @Test
    @Tag("integration")
    @Disabled("Test de integración, lento y requiere internet.")
    void getTeamInfoByName_whenTeamIsNotFound_shouldThrowException() {
        teamService = new TeamService();
        String teamName = "Equipo Que No Existe 12345";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            teamService.getTeamInfoByName(teamName);
        }, "Debería lanzar una excepción si el equipo no se encuentra.");
    }
}
