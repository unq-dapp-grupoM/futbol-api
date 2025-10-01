package com.dapp.futbol_api.service;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dapp.futbol_api.model.dto.TeamDTO;
import com.dapp.futbol_api.model.dto.TeamPlayerDTO;

@Service
public class TeamService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    public TeamDTO getTeamInfoByName(String teamName) {
        try (Playwright playwright = Playwright.create()) {
            Page page = createPage(playwright);

            // 2. Search for the team
            performSearch(page, teamName);

            // 3. Find and click the first team result
            // Los equipos suelen estar en el segundo bloque de resultados
            Locator firstResult = page.locator("#search-result .search-result:nth-of-type(2) tbody tr:nth-child(2) a")
                    .first();
            try {
                firstResult.waitFor(new Locator.WaitForOptions().setTimeout(10000));
            } catch (Exception e) {
                // Si no está en el segundo, buscar en el primero como fallback
                log.warn("Team not found in the second result block, trying the first one.");
                firstResult = page.locator("#search-result .search-result:first-of-type tbody tr:nth-child(2) a")
                        .first();
                firstResult.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            }

            firstResult.click();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            // 4. Extract team and squad data
            TeamDTO teamDTO = new TeamDTO();
            teamDTO.setName(extractText(page, "h1.team-header-name"));
            teamDTO.setSquad(scrapeSquadData(page));

            return teamDTO;
        } catch (Exception e) {
            log.error("An error occurred during scraping for team: {}", teamName, e);
            throw new RuntimeException("Failed to fetch data from whoscored.com", e);
        }
    }

    private List<TeamPlayerDTO> scrapeSquadData(Page page) {
        List<TeamPlayerDTO> squad = new ArrayList<>();
        Locator squadTableBody = page.locator("#player-table-statistics-body");
        if (!squadTableBody.isVisible()) {
            log.warn("Squad statistics table body not found.");
            return squad;
        }

        List<Locator> playerRows = squadTableBody.locator("tr").all();

        for (Locator row : playerRows) {
            String name = row.locator("td:nth-child(1) a.player-link span.iconize-icon-left").innerText();
            String age = row.locator("td:nth-child(1) span.player-meta-data:nth-of-type(1)").innerText();
            String position = row.locator("td:nth-child(1) span.player-meta-data:nth-of-type(2)").innerText();

            TeamPlayerDTO player = TeamPlayerDTO.builder()
                    .name(name).age(age).position(position.replace(",", "").trim())
                    .height(row.locator("td:nth-child(3)").innerText())
                    .weight(row.locator("td:nth-child(4)").innerText())
                    .apps(row.locator("td:nth-child(5)").innerText())
                    .minsPlayed(row.locator("td:nth-child(6)").innerText())
                    .goals(row.locator("td:nth-child(7)").innerText())
                    .assists(row.locator("td:nth-child(8)").innerText())
                    .yellowCards(row.locator("td:nth-child(9)").innerText())
                    .redCards(row.locator("td:nth-child(10)").innerText())
                    .shotsPerGame(row.locator("td:nth-child(11)").innerText())
                    .passSuccess(row.locator("td:nth-child(12)").innerText())
                    .aerialsWonPerGame(row.locator("td:nth-child(13)").innerText())
                    .manOfTheMatch(row.locator("td:nth-child(14)").innerText())
                    .rating(row.locator("td:nth-child(15)").innerText())
                    .build();
            squad.add(player);
        }
        return squad;
    }
}
