package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.PlayerDTO;
import com.dapp.futbol_api.model.dto.PlayerMatchStatsDTO;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService extends AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    public PlayerDTO getPlayerInfoByName(String playerName) {
        try (Playwright playwright = Playwright.create()) {
            Page page = createPage(playwright);

            // 2. Search for the player
            performSearch(page, playerName);

            // 3. Click on the first result
            // Selector corregido para apuntar a la primera fila de datos (la que contiene
            // 'td') dentro del contenedor de "Jugadores:".
            Locator firstResult = page
                    .locator("div.search-result:has(h2:text('Jugadores:')) >> tbody tr:nth-child(2) >> a")
                    .first();
            try {
                // ¡Solución! Esperar explícitamente a que el resultado sea visible.
                firstResult.waitFor(new Locator.WaitForOptions().setTimeout(15000)); // Esperar hasta 15 segundos
            } catch (Exception e) {
                log.error("Player '{}' not found in search results or timed out.", playerName);
                throw new IllegalArgumentException("Player with name '" + playerName + "' not found.");
            }
            firstResult.click();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            // 4. Scrape player data
            PlayerDTO playerDTO = scrapePlayerData(page);

            // 5. Navigate to Match Statistics and scrape data
            page.locator("//a[text()='Estadísticas del Partido']").click();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            playerDTO.setMatchStats(scrapePlayerMatchStats(page));

            return playerDTO;
        } catch (Exception e) {
            log.error("An error occurred during scraping for player: {}", playerName, e);
            throw new RuntimeException("Failed to fetch data from whoscored.com", e);
        }
    }

    public PlayerDTO scrapePlayerData(Page page) {
        PlayerDTO player = new PlayerDTO();

        // ¡Solución! Esperar a que el contenedor principal de la información del
        // jugador esté visible.
        Locator playerInfoContainer = page.locator("div.col12-lg-10.col12-m-10.col12-s-9.col12-xs-8");
        playerInfoContainer.waitFor(new Locator.WaitForOptions().setTimeout(10000));

        // Selector corregido para el nombre del jugador
        player.setName(extractText(page, "h1.header-name"));
        player.setCurrentTeam(extractText(page, "div:has(span.info-label:text-is('Equipo Actual:')) > a"));

        // Usamos el contenedor principal para acotar la búsqueda de los demás datos.
        player.setShirtNumber(extractValueFromInfoDiv(playerInfoContainer, "Número de Dorsal"));
        String ageText = extractValueFromInfoDiv(playerInfoContainer, "Edad");
        if (!ageText.equals(NOT_FOUND)) {
            // Extraer solo el número de la edad, ej: "38" de "38 años (24-06-1987)"
            player.setAge(ageText.split(" ")[0].trim());
        } else {
            player.setAge(ageText);
        }
        player.setHeight(extractValueFromInfoDiv(playerInfoContainer, "Altura"));
        // El nombre de la nacionalidad está en el texto, no en un atributo 'title'
        player.setNationality(extractText(playerInfoContainer,
                "div.col12-lg-6:has(span.info-label:text-is('Nacionalidad:')) > span.iconize"));

        // Las posiciones pueden ser múltiples spans, así que los unimos.
        Locator positionsContainer = playerInfoContainer
                .locator("div:has(span.info-label:text-is('Posiciones:')) > span:not(.info-label)");
        if (positionsContainer.isVisible()) {
            String positions = positionsContainer.locator("span").all().stream()
                    .map(Locator::innerText)
                    .collect(Collectors.joining(" "));
            player.setPositions(positions);
        } else {
            player.setPositions(NOT_FOUND);
        }
        return player;
    }

    private String extractValueFromInfoDiv(Locator context, String label) {
        try {
            // Buscamos el div específico dentro del contexto (playerInfoContainer)
            String selector = String.format("div.col12-lg-6:has(span.info-label:text-is('%s:'))", label);
            String fullText = context.locator(selector).first().innerText();
            return fullText.replace(label + ":", "").trim();
        } catch (Exception e) {
            log.warn("Could not extract value for label '{}'", label);
            return NOT_FOUND;
        }
    }

    private List<PlayerMatchStatsDTO> scrapePlayerMatchStats(Page page) {
        List<PlayerMatchStatsDTO> matchStats = new ArrayList<>();
        Locator statsTableBody = page.locator("#player-table-statistics-body");
        if (!statsTableBody.isVisible()) {
            log.warn("Player match statistics table not found.");
            return matchStats;
        }

        List<Locator> matchRows = statsTableBody.locator("tr").all();
        for (Locator row : matchRows) {
            // Selector para el enlace que contiene tanto el oponente como el resultado.
            Locator opponentLink = row.locator("td:nth-child(1) a.player-match-link");
            String fullOpponentText = opponentLink.innerText();
            String score = opponentLink.locator("span.scoreline").innerText();
            String opponent = fullOpponentText.replace(score, "").trim();

            PlayerMatchStatsDTO match = PlayerMatchStatsDTO.builder()
                    .opponent(opponent)
                    .score(score)
                    .date(row.locator("td:nth-child(3)").innerText())
                    .position(row.locator("td:nth-child(4)").innerText())
                    .minsPlayed(row.locator("td:nth-child(5)").innerText())
                    .goals(row.locator("td:nth-child(6)").innerText())
                    .assists(row.locator("td:nth-child(7)").innerText())
                    .yellowCards(row.locator("td:nth-child(8)").innerText())
                    .redCards(row.locator("td:nth-child(9)").innerText())
                    .shots(row.locator("td:nth-child(10)").innerText())
                    .passSuccess(row.locator("td:nth-child(11)").innerText())
                    .aerialsWon(row.locator("td:nth-child(12)").innerText())
                    .rating(row.locator("td:nth-child(13)").innerText())
                    .build();
            matchStats.add(match);
        }
        return matchStats;
    }

}
