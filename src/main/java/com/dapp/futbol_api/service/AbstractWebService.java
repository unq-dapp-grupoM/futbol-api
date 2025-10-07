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
        try {
            log.info("Waiting for cookie banner...");

            // The cookie banner might be inside an iframe. Let's check for that.
            FrameLocator iframe = page.frameLocator("[title='SP Consent Message']");
            Locator acceptButton;

            // More robust pattern for button text
            Pattern acceptPattern = Pattern.compile("ACCEPT ALL|Aceptar todo|Consent|I Agree",
                    Pattern.CASE_INSENSITIVE);

            if (iframe.locator("body").isVisible()) {
                log.info("Cookie banner is inside an iframe. Searching within it.");
                acceptButton = iframe.getByRole(AriaRole.BUTTON,
                        new FrameLocator.GetByRoleOptions().setName(acceptPattern));
            } else {
                log.info("Searching for cookie banner in the main page.");
                acceptButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(acceptPattern));
            }

            acceptButton.waitFor(new Locator.WaitForOptions().setTimeout(15000)); // Increased wait time to 15s
            acceptButton.click();
            log.info("Cookie banner accepted.");
        } catch (Exception e) {
            log.warn(
                    "Cookie button not found or could not be clicked within the timeout. This might be okay. Continuing... Error: "
                            + e.getMessage());
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