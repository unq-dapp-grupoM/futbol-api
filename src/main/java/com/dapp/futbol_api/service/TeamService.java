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
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='search-result']")));
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

            // 5. Navigate to the "Fixtures" page
            log.info("Navigating to Fixtures page...");
            WebElement fixturesLink = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//div[@id='sub-navigation']//a[text()='Encuentros']")));
            fixturesLink.click();

            // 6. Wait for the fixtures page to load and extract data
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("team-fixtures")));
            log.info("Fixtures page loaded. Extracting match data...");

            teamDTO.setFixture(scrapeFixtureData(driver, teamDTO.getName()));
            return teamDTO;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private List<GameMatchDTO> scrapeFixtureData(WebDriver driver, String teamName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<WebElement> matchRows;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#team-fixtures .divtable-body")));
            matchRows = driver.findElements(By.cssSelector("#team-fixtures .divtable-row.item"));
            if (matchRows.isEmpty()) {
                throw new IllegalStateException("No match rows found on the fixtures page.");
            }
        } catch (Exception e) {
            log.error("No matches found in the fixture for {}.", teamName, e);
            return List.of(GameMatchDTO.builder().cup("No match data found.").build());
        }

        List<GameMatchDTO> allMatches = new ArrayList<>();
        for (WebElement matchRow : matchRows) {
            GameMatchDTO fixture = new GameMatchDTO();
            fixture.setCup(extractText(matchRow, By.cssSelector("a.tournament-link")));
            fixture.setDate(extractDate(matchRow));
            fixture.setScore(extractText(matchRow, By.cssSelector("div.result a")));

            fixture.setResult("Pendeing Match");
            if (!matchRow.findElements(By.cssSelector("a.box.w")).isEmpty()) {
                fixture.setResult("Match Won");
            } else if (!matchRow.findElements(By.cssSelector("a.box.l")).isEmpty()) {
                fixture.setResult("Lost Match");
            } else if (!matchRow.findElements(By.cssSelector("a.box.d")).isEmpty()) {
                fixture.setResult("Tied Match");
            }

            String homeTeam = extractText(matchRow, By.cssSelector("div.team.home a.team-link"));
            String awayTeam = extractText(matchRow, By.cssSelector("div.team.away a.team-link"));
            String homeRedCards = extractRedCard(matchRow, "div.team.home");
            String awayRedCards = extractRedCard(matchRow, "div.team.away");

            if (homeTeam.equalsIgnoreCase(teamName)) {
                fixture.setActualTeam(homeTeam);
                fixture.setRivalTeam(awayTeam);
                fixture.setRedCardsActualTeam(homeRedCards);
                fixture.setRedCardsRivalTeam(awayRedCards);
            } else {
                fixture.setActualTeam(awayTeam);
                fixture.setRivalTeam(homeTeam);
                fixture.setRedCardsActualTeam(awayRedCards);
                fixture.setRedCardsRivalTeam(homeRedCards);
            }
            allMatches.add(fixture);
        }

        return allMatches;
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

    private String extractDate(WebElement matchRow) {
        List<WebElement> dateElements = matchRow.findElements(By.cssSelector("div.date"));
        for (WebElement dateElement : dateElements) {
            String dateText = dateElement.getText();
            if (dateText != null && !dateText.trim().isEmpty()) {
                return dateText;
            }
        }
        log.warn("Could not find date for a match row.");
        return "Not found";
    }

    private String extractText(SearchContext context, By locator) {
        try {
            return context.findElement(locator).getText();
        } catch (Exception e) {
            log.warn("Could not find element with locator: {}", locator);
            return "Not found";
        }
    }

    private String extractRedCard(WebElement matchRow, String teamSelector) {
        try {
            return matchRow.findElement(By.cssSelector(teamSelector + " span.rcard")).getText();
        } catch (Exception e) {
            return "0";
        }
    }
}
