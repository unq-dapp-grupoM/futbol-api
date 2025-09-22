package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.PlayerDTO;
import com.dapp.futbol_api.model.dto.PlayerMatchStatsDTO;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private static final String BASE_URL = "https://es.whoscored.com/";

    public PlayerDTO getPlayerInfoByName(String playerName) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
        WebDriver driver = new ChromeDriver(options);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            driver.get(BASE_URL);

            // 1. Manejar el banner de cookies
            try {
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[text()='Aceptar todo']"))).click();
                log.info("Banner de cookies aceptado.");
            } catch (Exception e) {
                log.warn("No se encontró o no se pudo hacer clic en el botón de cookies. Continuando...");
            }

            // 2. Buscar el jugador en la barra de búsqueda
            log.info("Buscando al jugador: {}", playerName);
            WebElement searchBox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("input[placeholder='Buscar campeonatos, equipos y jugadores']")));
            searchBox.sendKeys(playerName);
            searchBox.sendKeys(Keys.ENTER);

            // 3. Esperar los resultados y hacer clic en el primero
            log.info("Esperando resultados de la búsqueda...");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='search-result']")));
            log.info("Resultados encontrados. Haciendo clic en el primer jugador.");
            WebElement firstResult = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@class='search-result']/table/tbody/tr[2]/td[1]/a")));
            firstResult.click();

            // 4. Esperar a que la página del jugador cargue y extraer datos
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='header']")));
            log.info("Página del jugador cargada. Extrayendo datos...");

            WebElement headerContainer = driver.findElement(By.xpath("//div[@class='header']"));
            PlayerDTO playerDTO = scrapePlayerData(headerContainer);

            // 5. Navegar a la pestaña "Estadísticas del Partido"
            log.info("Navegando a la pestaña 'Estadísticas del Partido'...");
            try {
                WebElement statsLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@id='sub-navigation']//a[text()='Estadísticas del Partido']")));
                statsLink.click();

                // 6. Esperar a que la tabla de estadísticas cargue y extraer datos
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("statistics-table-summary-matches")));
                log.info("Página de estadísticas cargada. Extrayendo datos de partidos...");
                playerDTO.setMatchStats(scrapePlayerMatchStats(driver));
            } catch (Exception e) {
                log.error("No se pudo navegar o extraer las estadísticas de los partidos.", e);
            }
            return playerDTO;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public PlayerDTO scrapePlayerData(WebElement headerContainer) {
        PlayerDTO player = new PlayerDTO();

        // El contexto de búsqueda principal será el div 'header'
        player.setName(extractText(headerContainer, By.xpath(".//h1[@class='header-name']")));

        // El resto de la información está en un sub-contenedor.
        WebElement infoContainer = headerContainer.findElement(By.xpath(".//div[contains(@class, 'col12-lg-10')]"));

        // Equipo Actual (es un enlace, caso especial)
        player.setCurrentTeam(
                extractText(infoContainer, By.xpath(".//span[contains(text(),'Equipo Actual')]/following-sibling::a")));

        // Número de Dorsal
        player.setShirtNumber(extractValueFromInfoDiv(infoContainer, "Número de Dorsal"));

        // Edad
        String ageText = extractValueFromInfoDiv(infoContainer, "Edad");
        if (!ageText.equals("No encontrado")) {
            player.setAge(ageText.split(" ")[0]);
        } else {
            player.setAge(ageText);
        }

        // Altura
        player.setHeight(extractValueFromInfoDiv(infoContainer, "Altura"));

        player.setNationality(extractText(infoContainer,
                By.xpath(".//span[contains(text(),'Nacionalidad')]/following-sibling::span")));

        // Posiciones
        List<WebElement> positionElements = infoContainer
                .findElements(By.xpath(".//span[contains(text(),'Posiciones')]/following-sibling::span/span"));
        String positions = positionElements.stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(" "));
        player.setPositions(positions.isEmpty() ? "No encontrado" : positions);
        return player;
    }

    private String extractText(SearchContext context, By locator) {
        try {
            return context.findElement(locator).getText();
        } catch (Exception e) {
            log.warn("No se pudo encontrar el elemento con el localizador: {}", locator);
            return "No encontrado";
        }
    }

    /**
     * Extrae el valor de un div de información que sigue el patrón "Etiqueta:
     * Valor".
     * 
     * @param infoContainer El WebElement que contiene los divs de información.
     * @param label         La etiqueta de texto a buscar (ej. "Altura").
     * @return El valor extraído como String, o "No encontrado".
     */
    private String extractValueFromInfoDiv(WebElement infoContainer, String label) {
        String fullText = extractText(infoContainer, By.xpath(".//span[contains(text(),'" + label + "')]/parent::div"));
        if (!fullText.equals("No encontrado")) {
            return fullText.replace(label + ":", "").trim();
        }
        return fullText;
    }

    private List<PlayerMatchStatsDTO> scrapePlayerMatchStats(WebDriver driver) {
        List<PlayerMatchStatsDTO> matchStats = new ArrayList<>();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement statsTableBody = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("player-table-statistics-body")));
            List<WebElement> matchRows = statsTableBody.findElements(By.tagName("tr"));

            for (WebElement row : matchRows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 13)
                    continue; // Skip malformed rows

                WebElement opponentCell = cells.get(0).findElement(By.tagName("a"));
                String fullOpponentText = opponentCell.getText();
                String score = extractText(opponentCell, By.cssSelector("span.scoreline"));
                String opponent = fullOpponentText.replace(score, "").trim();

                PlayerMatchStatsDTO match = PlayerMatchStatsDTO.builder()
                        .opponent(opponent)
                        .score(score)
                        .date(cells.get(2).getText())
                        .position(cells.get(3).getText())
                        .minsPlayed(cells.get(4).getText())
                        .goals(cells.get(5).getText())
                        .assists(cells.get(6).getText())
                        .yellowCards(cells.get(7).getText())
                        .redCards(cells.get(8).getText())
                        .shots(cells.get(9).getText())
                        .passSuccess(cells.get(10).getText())
                        .aerialsWon(cells.get(11).getText())
                        .rating(cells.get(12).getText())
                        .build();
                matchStats.add(match);
            }
        } catch (Exception e) {
            log.error("Error al extraer las estadísticas de los partidos del jugador.", e);
        }
        return matchStats;
    }
}