package com.dapp.futbol_api.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebService.class);
    protected static final String BASE_URL = "https://es.whoscored.com/";
    protected static final String NOT_FOUND = "Not found";

    protected Page createPage(Playwright playwright) {
        // ¡Solución! Lanzar el navegador en modo visible para depuración.
        // setHeadless(false) abre una ventana del navegador.
        // setSlowMo(50) añade una pequeña pausa de 50ms entre acciones para que sea más
        // fácil de seguir.
        Browser browser = playwright.chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
        Page page = browser.newPage();

        // Aceptar cookies antes de cada navegación
        page.navigate(BASE_URL);
        try {
            // Usar getByRole es más robusto para encontrar el botón de cookies.
            Locator acceptButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Aceptar todo"));
            acceptButton.waitFor(new Locator.WaitForOptions().setTimeout(7000)); // Aumentar la espera
            acceptButton.click();
            log.info("Cookie banner accepted.");
        } catch (Exception e) {
            log.warn("Cookie button not found or could not be clicked. Continuing...");
        }
        return page;
    }

    protected void performSearch(Page page, String searchTerm) {
        log.info("Searching for: {}", searchTerm);
        // Usar getByPlaceholder es más robusto que un selector CSS genérico.
        Locator searchInput = page.getByPlaceholder("Buscar campeonatos, equipos y jugadores");
        // Hacer clic primero para asegurar que el input tenga el foco.
        searchInput.click();
        searchInput.fill(searchTerm);
        searchInput.press("Enter");
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    protected String extractText(Page page, String selector) {
        try {
            return page.locator(selector).first().innerText();
        } catch (Exception e) {
            log.warn("Could not find element with selector: {}", selector);
            return NOT_FOUND;
        }
    }

    protected String extractText(Locator context, String selector) {
        try {
            // Playwright esperará automáticamente a que el elemento aparezca.
            return context.locator(selector).first().innerText();
        } catch (Exception e) {
            log.warn("Could not find element with selector '{}' in the given context", selector);
            return NOT_FOUND;
        }
    }

    protected String extractAttribute(Page page, String selector, String attribute) {
        try {
            return page.locator(selector).first().getAttribute(attribute);
        } catch (Exception e) {
            log.warn("Could not find attribute '{}' for element with selector: {}", attribute, selector);
            return NOT_FOUND;
        }
    }
}