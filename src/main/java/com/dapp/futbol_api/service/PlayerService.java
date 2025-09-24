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
    private static final String NOT_FOUND = "Not found";

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

            // 1. Handle cookie banner
            try {
                wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[text()='Aceptar todo']"))).click();
                log.info("Cookie banner accepted.");
            } catch (Exception e) {
                log.warn("Cookie button not found or could not be clicked. Continuing...");
            }

            // 2. Search for the player in the search bar
            log.info("Searching for player: {}", playerName);
            WebElement searchBox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("input[placeholder='Buscar campeonatos, equipos y jugadores']")));
            searchBox.sendKeys(playerName);
            searchBox.sendKeys(Keys.ENTER);

            // 3. Wait for results and click the first one
            log.info("Waiting for search results...");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='search-result']")));
            log.info("Results found. Clicking on the first player.");
            WebElement firstResult = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@class='search-result']/table/tbody/tr[2]/td[1]/a")));
            firstResult.click();

            // 4. Wait for the player page to load and extract data
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='header']")));
            log.info("Player page loaded. Extracting data...");

            WebElement headerContainer = driver.findElement(By.xpath("//div[@class='header']"));
            PlayerDTO playerDTO = scrapePlayerData(headerContainer);

            // 5. Navigate to the "Match Statistics" tab
            log.info("Navigating to 'Match Statistics' tab...");
            try {
                WebElement statsLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@id='sub-navigation']//a[text()='Estadísticas del Partido']")));
                statsLink.click();

                // 6. Wait for the statistics table to load and extract data
                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("statistics-table-summary-matches")));
                log.info("Statistics page loaded. Extracting match data...");
                playerDTO.setMatchStats(scrapePlayerMatchStats(driver));
            } catch (Exception e) {
                log.error("Could not navigate to or extract match statistics.", e);
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

        // The main search context will be the 'header' div
        player.setName(extractText(headerContainer, By.xpath(".//h1[@class='header-name']")));

        // The rest of the information is in a sub-container.
        WebElement infoContainer = headerContainer.findElement(By.xpath(".//div[contains(@class, 'col12-lg-10')]"));

        // Current Team (it's a link, special case)
        player.setCurrentTeam(
                extractText(infoContainer, By.xpath(".//span[contains(text(),'Equipo Actual')]/following-sibling::a")));

        // Shirt Number
        player.setShirtNumber(extractValueFromInfoDiv(infoContainer, "Número de Dorsal"));

        // Age
        String ageText = extractValueFromInfoDiv(infoContainer, "Edad");
        if (!ageText.equals(NOT_FOUND)) {
            player.setAge(ageText.split(" ")[0]);
        } else {
            player.setAge(ageText);
        }

        // Height
        player.setHeight(extractValueFromInfoDiv(infoContainer, "Altura"));

        player.setNationality(extractText(infoContainer,
                By.xpath(".//span[contains(text(),'Nacionalidad')]/following-sibling::span")));

        // Positions
        List<WebElement> positionElements = infoContainer
            .findElements(By.xpath(".//span[contains(text(),'Posiciones')]/following-sibling::span/span"));
        String positions = positionElements.stream()
            .map(WebElement::getText)
            .collect(Collectors.joining(" "));
        player.setPositions(positions.isEmpty() ? NOT_FOUND : positions);
        return player;
        }

    private String extractText(SearchContext context, By locator) {
        try {
            return context.findElement(locator).getText();
        } catch (Exception e) {
            log.warn("Could not find element with locator: {}", locator);
            return NOT_FOUND;
        }
    }

    /**
     * Extracts the value from an information div that follows the "Label: Value"
     * pattern.
     * 
     * @param infoContainer The WebElement containing the information divs.
     * @param label         The text label to search for (e.g., "Height").
     * @return The extracted value as a String, or "Not found".
     */
    private String extractValueFromInfoDiv(WebElement infoContainer, String label) {
        String fullText = extractText(infoContainer, By.xpath(".//span[contains(text(),'" + label + "')]/parent::div"));
        if (!fullText.equals(NOT_FOUND)) {
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
            log.error("Error extracting player match statistics.", e);
        }
        return matchStats;
    }
}