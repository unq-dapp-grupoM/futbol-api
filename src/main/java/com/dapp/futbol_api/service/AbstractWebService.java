package com.dapp.futbol_api.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.AriaRole;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.List;

public abstract class AbstractWebService {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebService.class);
    protected static final String BASE_URL = "https://es.whoscored.com/";
    protected static final String NOT_FOUND = "Not found";

    @Autowired
    private Browser browser;

    protected Page createPage() {
        // Create a browser context with options that simulate a real user.
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setUserAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .setViewportSize(1920, 1080) // Common window size
                .setLocale("es-ES") // Set locale
                .setTimezoneId("America/Argentina/Buenos_Aires"));

        Page page = context.newPage();

        page.navigate(BASE_URL);
        handleCookieBanner(page);
        return page;
    }

    private void handleCookieBanner(Page page) {
        boolean isRender = System.getenv("RENDER") != null; // Render define esta variable automáticamente
        int timeout = isRender ? 25000 : 15000; // mayor tolerancia en Render

        try {
            log.info("Waiting for cookie banner...");

            // Pequeña espera para permitir que carguen scripts lentos (solo Render)
            if (isRender) {
                page.waitForTimeout(2000);
            }

            // Patrón flexible para distintos idiomas o textos de botones
            Pattern acceptPattern = Pattern.compile("ACCEPT ALL|Aceptar todo|Consent|I Agree",
                    Pattern.CASE_INSENSITIVE);

            Locator acceptButton = null;

            // Intentar detectar iframe de consentimiento
            FrameLocator iframe = page.frameLocator("[title='SP Consent Message']");
            boolean iframeVisible = false;
            try {
                iframeVisible = iframe.locator("body").isVisible();
            } catch (Exception ignored) {
                // si no existe, seguimos
            }

            if (iframeVisible) {
                log.info("Cookie banner found inside iframe. Searching within it.");
                acceptButton = iframe.getByRole(AriaRole.BUTTON,
                        new FrameLocator.GetByRoleOptions().setName(acceptPattern));
            } else {
                log.info("Searching for cookie banner in the main page.");
                acceptButton = page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(acceptPattern));
            }

            // Verificamos si el botón es visible antes de esperar o hacer click
            if (acceptButton != null && acceptButton.isVisible()) {
                acceptButton.click();
                log.info("Cookie banner accepted successfully.");
            } else {
                log.warn("Cookie banner not visible, skipping acceptance step (likely headless environment).");
            }

        } catch (com.microsoft.playwright.TimeoutError e) {
            log.warn("Cookie banner not found within {}ms, continuing without accepting. (TimeoutError)", timeout);
        } catch (Exception e) {
            log.warn("Unexpected error while handling cookie banner: {}", e.getMessage());
        }
    }

    protected void performSearch(Page page, String searchTerm) {
        log.info("Searching for: {}", searchTerm);
        // Using getByPlaceholder is more robust than a generic CSS selector.
        Locator searchInput = page.getByPlaceholder("Buscar campeonatos, equipos y jugadores");
        // Click first to ensure the input has focus.
        searchInput.click();
        searchInput.fill(searchTerm);
        searchInput.press("Enter");
        // Wait for the network to be idle, but with a longer timeout for production
        // environments.
        log.info("Waiting for search results to load...");
        page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(30000));
        log.info("Search page loaded.");
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
            // Playwright will automatically wait for the element to appear.
            return context.locator(selector).first().innerText();
        } catch (Exception e) {
            log.warn("Could not find element with selector '{}' in the given context", selector);
            return NOT_FOUND;
        }
    }

    protected String extractAttribute(Page page, String selector, String attribute) {
        try {
            String value = page.locator(selector).first().getAttribute(attribute);
            if (value == null) {
                log.warn("Attribute '{}' not found for element with selector: {}", attribute, selector);
                return NOT_FOUND;
            }
            return value;
        } catch (Exception e) {
            log.warn("Could not find attribute '{}' for element with selector: {}", attribute, selector);
            return NOT_FOUND;
        }
    }

}