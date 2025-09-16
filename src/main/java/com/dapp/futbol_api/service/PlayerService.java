package com.dapp.futbol_api.service;

import com.dapp.futbol_api.model.dto.PlayerDTO;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private static final String BASE_URL = "https://es.whoscored.com/";

    public PlayerDTO getPlayerInfoByName(String playerName) {
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            driver.get(BASE_URL);

            // 1. Manejar el banner de cookies
            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".css-1wc0q5e"))).click();
                log.info("Banner de cookies aceptado.");
            } catch (Exception e) {
                log.warn("No se encontró o no se pudo hacer clic en el botón de cookies. Continuando...");
            }

            // 2. Buscar el jugador en la barra de búsqueda
            log.info("Buscando al jugador: {}", playerName);
            WebElement searchBox = wait.until(
                    ExpectedConditions
                            .visibilityOfElementLocated(By.cssSelector("input.SearchBar-module_searchBox__l4aAt")));
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
            return scrapePlayerData(headerContainer);

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public PlayerDTO scrapePlayerData(WebElement headerContainer) {
        PlayerDTO jugador = new PlayerDTO();

        // El contexto de búsqueda principal será el div 'header'
        jugador.setNombre(extractText(headerContainer, By.xpath(".//h1[@class='header-name']")));

        // El resto de la información está en un sub-contenedor.
        WebElement infoContainer = headerContainer.findElement(By.xpath(".//div[contains(@class, 'col12-lg-10')]"));

        // Equipo Actual (es un enlace, caso especial)
        jugador.setEquipoActual(
                extractText(infoContainer, By.xpath(".//span[contains(text(),'Equipo Actual')]/following-sibling::a")));

        // Número de Dorsal
        jugador.setNumeroDorsal(extractValueFromInfoDiv(infoContainer, "Número de Dorsal"));

        // Edad
        String ageText = extractValueFromInfoDiv(infoContainer, "Edad");
        if (!ageText.equals("No encontrado")) {
            jugador.setEdad(ageText.split(" ")[0]); // Tomamos solo el número de la edad
        } else {
            jugador.setEdad(ageText);
        }

        // Altura
        jugador.setAltura(extractValueFromInfoDiv(infoContainer, "Altura"));

        // Nacionalidad (es un span separado, caso especial)
        jugador.setNacionalidad(extractText(infoContainer,
                By.xpath(".//span[contains(text(),'Nacionalidad')]/following-sibling::span")));

        // Posiciones
        List<WebElement> positionElements = infoContainer
                .findElements(By.xpath(".//span[contains(text(),'Posiciones')]/following-sibling::span/span"));
        String posiciones = positionElements.stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(" "));
        jugador.setPosiciones(posiciones.isEmpty() ? "No encontrado" : posiciones);
        return jugador;
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
            // Reemplaza la etiqueta y los dos puntos para quedarse solo con el valor.
            return fullText.replace(label + ":", "").trim();
        }
        return fullText;
    }
}