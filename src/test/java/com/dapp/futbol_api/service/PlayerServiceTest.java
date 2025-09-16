package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.PlayerDTO;
import java.time.Duration;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerServiceTest {

    private PlayerService playerService;

    @BeforeAll
    static void setUpDriver() {
    }

    @Test
    @Tag("unit")
    void getPlayerInfoFromLocalFile_shouldParseAllDataCorrectly() throws MalformedURLException {
        // Arrange
        playerService = new PlayerService();
        String playerName = "Lionel Messi";

        // --- Carga del HTML local ---
        // El archivo ahora se carga desde la carpeta de recursos de test.
        Path resourcePath = Paths.get("src", "test", "resources", "player_messi.html");
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
        // -----------------------------

        try {
            // Act
            driver.get(resourceUrl.toString());
            // Añadimos una espera explícita para asegurar que el elemento esté cargado.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement headerContainer = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='header']")));
            // Llamamos directamente al método de parsing
            PlayerDTO player = playerService.scrapePlayerData(headerContainer);

            // Assert
            assertNotNull(player, "El DTO del jugador no debería ser nulo.");
            assertEquals(playerName, player.getNombre());
            assertEquals("Inter Miami CF", player.getEquipoActual());
            assertEquals("10", player.getNumeroDorsal());
            assertNotNull(player.getEdad(), "La edad no debería ser nula.");
            assertFalse(player.getEdad().isEmpty(), "La edad no debería estar vacía.");
            assertEquals("170cm", player.getAltura());
            assertEquals("Argentina", player.getNacionalidad());
            assertTrue(player.getPosiciones().contains("Mediapunta"), "Debería contener la posición Mediapunta.");
            assertTrue(player.getPosiciones().contains("Delantero"), "Debería contener la posición Delantero.");

        } finally {
            driver.quit();
        }
    }

    @Test
    @Tag("integration")
    @Disabled("Test de integración, lento y requiere internet.")
    void getPlayerInfoByName_shouldParseAllDataCorrectly() {
        // Arrange
        playerService = new PlayerService();
        String playerName = "Lionel Messi";

        // Act
        PlayerDTO player = playerService.getPlayerInfoByName(playerName);

        // Assert
        assertNotNull(player, "El DTO del jugador no debería ser nulo.");
        assertEquals(playerName, player.getNombre());
        assertEquals("Inter Miami CF", player.getEquipoActual());
        assertEquals("10", player.getNumeroDorsal());
        assertNotNull(player.getEdad(), "La edad no debería ser nula.");
        assertFalse(player.getEdad().isEmpty(), "La edad no debería estar vacía.");
        assertEquals("170cm", player.getAltura());
        assertEquals("Argentina", player.getNacionalidad());
        // El scraping real une las posiciones con un espacio, ej: "Mediapunta (Central,
        // Derecho), Delantero"
        assertTrue(player.getPosiciones().contains("Mediapunta"), "Debería contener la posición Mediapunta.");
        assertTrue(player.getPosiciones().contains("Delantero"), "Debería contener la posición Delantero.");
    }

    @Test
    @Tag("integration")
    @Disabled("Test de integración, lento y requiere internet.")
    void getPlayerInfoByName_whenPlayerIsNotFound_shouldThrowException() {
        // Arrange
        playerService = new PlayerService();
        String playerName = "Jugador Que No Existe Para Nada 12345";

        // Act
        // El servicio actual no lanza una excepción específica, sino que probablemente
        // falle
        // por un error de Selenium al no encontrar el resultado. Un test más robusto
        // esperaría una excepción custom.
        assertThrows(Exception.class, () -> {
            playerService.getPlayerInfoByName(playerName);
        }, "Debería lanzar una excepción si el jugador no se encuentra.");
    }
}
