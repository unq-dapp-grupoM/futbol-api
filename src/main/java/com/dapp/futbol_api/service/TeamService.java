package com.dapp.futbol_api.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dapp.futbol_api.model.dto.TeamDTO;
import com.dapp.futbol_api.model.dto.TeamPlayerDTO;
import com.dapp.futbol_api.model.dto.GameMatchDTO;

@Service
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);
    private static final String BASE_URL = "https://es.whoscored.com/";

    public TeamDTO getTeamInfoByName(String teamName) {
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

            // 2. Search for the team in the search bar
            log.info("Searching for team: {}", teamName);
            WebElement searchBox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                            "input[placeholder='Buscar campeonatos, equipos y jugadores']")));
            searchBox.sendKeys(teamName);
            searchBox.sendKeys(Keys.ENTER);

            // 3. Wait for results and click the first team
            log.info("Waiting for search results...");
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='search-result']")));
            } catch (TimeoutException e) {
                log.error("Timeout waiting for team page to load.");
                throw new IllegalArgumentException("Team of name \'" + teamName + "\' not found or page took too long to load.");
            }
            log.info("Results found. Clicking on the first team.");
            WebElement firstResult = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@class='search-result']/table/tbody/tr[2]/td[1]/a")));
            firstResult.click();

            // 4. Wait for the team page to load and extract squad data
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("team-squad-stats")));
            log.info("Team page loaded. Extracting squad data...");

            TeamDTO teamDTO = new TeamDTO();
            teamDTO.setName(extractText(driver, By.cssSelector("span.team-header-name")));
            teamDTO.setSquad(scrapeSquadData(driver));

            return teamDTO;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private List<TeamPlayerDTO> scrapeSquadData(WebDriver driver) {
        List<TeamPlayerDTO> squad = new ArrayList<>();
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement squadTable = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("top-player-stats-summary-grid")));
            List<WebElement> playerRows = squadTable.findElements(By.xpath(".//tbody/tr"));

            for (WebElement row : playerRows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 15)
                    continue;

                WebElement playerInfoCell = cells.get(0);
                String name = extractText(playerInfoCell, By.cssSelector("a.player-link > span"));

                List<WebElement> metaData = playerInfoCell.findElements(By.cssSelector("span > span.player-meta-data"));
                String age = "N/A";
                String position = "N/A";
                if (metaData.size() >= 2) {
                    age = metaData.get(0).getText();
                    position = metaData.get(1).getText().replace(",", "").trim();
                }

                TeamPlayerDTO player = TeamPlayerDTO.builder()
                        .name(name).age(age).position(position)
                        .height(cells.get(2).getText()).weight(cells.get(3).getText())
                        .apps(cells.get(4).getText()).minsPlayed(cells.get(5).getText())
                        .goals(cells.get(6).getText()).assists(cells.get(7).getText())
                        .yellowCards(cells.get(8).getText()).redCards(cells.get(9).getText())
                        .shotsPerGame(cells.get(10).getText()).passSuccess(cells.get(11).getText())
                        .aerialsWonPerGame(cells.get(12).getText()).manOfTheMatch(cells.get(13).getText())
                        .rating(cells.get(14).getText())
                        .build();

                squad.add(player);
            }
        } catch (Exception e) {
            log.error("Error extracting squad data.", e);
        }
        return squad;
    }

    private String extractText(SearchContext context, By locator) {
        try {
            return context.findElement(locator).getText();
        } catch (Exception e) {
            log.warn("Could not find element with locator: {}", locator);
            return "Not found";
        }
    }
}
