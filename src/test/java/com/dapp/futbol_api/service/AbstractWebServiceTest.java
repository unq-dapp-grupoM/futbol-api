package com.dapp.futbol_api.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractWebServiceTest {

    // A concrete implementation of the abstract class for testing purposes
    static class TestableWebService extends AbstractWebService {
    }

    private TestableWebService webService;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        webService = new TestableWebService();
        context = browser.newContext();
        page = context.newPage();

        page.setContent("<a id='test-link'>Click me</a>");
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void testCreatePageShouldAcceptCookiesWhenPresent() {
        // Arrange: Intercept the navigation to the base URL and provide mock HTML with
        // a cookie button
        context.route(AbstractWebService.BASE_URL, route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setContentType("text/html")
                    .setBody("<html><body><button>Aceptar todo</button></body></html>"));
        });

        // Act: We need to create a new page within a new context for this specific test
        try (Playwright testPlaywright = Playwright.create()) {
            Page testPage = webService.createPage(testPlaywright);

            // Assert: Check if the page content is loaded (the cookie button is gone)
            // The click action removes the button in this simple mock, so we check it's not
            // visible.
            assertThat(testPage.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Aceptar todo")))
                    .isHidden();
            testPage.close();
        }
    }

    @Test
    void testCreatePageShouldContinueWhenCookieBannerIsMissing() {
        // Arrange: Intercept navigation and provide HTML without a cookie button
        context.route(AbstractWebService.BASE_URL, route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setContentType("text/html")
                    .setBody("<html><body><h1>No cookie banner</h1></body></html>"));
        });

        // Act & Assert: The method should not throw an exception
        try (Playwright testPlaywright = Playwright.create()) {
            assertDoesNotThrow(() -> {
                Page testPage = webService.createPage(testPlaywright);
                testPage.close();
            });
        }
    }

    @Test
    void testPerformSearchShouldFillAndSubmit() {
        // Arrange
        page.setContent("<input placeholder='Buscar campeonatos, equipos y jugadores' />");

        // Act
        webService.performSearch(page, "Lionel Messi");

        // Assert
        Locator searchInput = page.getByPlaceholder("Buscar campeonatos, equipos y jugadores");
        assertThat(searchInput).hasValue("Lionel Messi");
    }

    @Test
    void testExtractTextShouldReturnTextWhenElementExists() {
        // Arrange
        page.setContent("<div id='test-div'>Hello World</div>");

        // Act
        String text = webService.extractText(page, "#test-div");

        // Assert
        assertEquals("Hello World", text);
    }

    @Test
    void testExtractTextShouldReturnNotFoundWhenElementIsMissing() {
        // Arrange
        page.setContent("<div></div>");

        // Act
        String text = webService.extractText(page, "#non-existent-div");

        // Assert
        assertEquals(AbstractWebService.NOT_FOUND, text);
    }

    @Test
    void testExtractTextWithLocatorContext() {
        // Arrange
        page.setContent("<div id='container'><span class='data'>Contained Text</span></div>");
        Locator container = page.locator("#container");

        // Act
        String text = webService.extractText(container, ".data");

        // Assert
        assertEquals("Contained Text", text);
    }

    @Test
    void testExtractAttributeShouldReturnAttributeWhenExists() {
        // Arrange
        page.setContent("<a id='test-link' href='/test-url'>Click me</a>");

        // Act
        String href = webService.extractAttribute(page, "#test-link", "href");

        // Assert
        assertEquals("/test-url", href);
    }

    @Test
    void testExtractAttributeShouldReturnNotFoundWhenMissing() {
        // Arrange
        page.setContent("<a id='test-link'>Click me</a>");

        // Act
        String href = webService.extractAttribute(page, "#test-link", "href");

        // Assert
        assertEquals(AbstractWebService.NOT_FOUND, href);
    }

    @Test
    void testCreatePageShouldHandleTimeoutException() {
        // Arrange: Intercept navigation and provide HTML where the button is never
        // visible
        context.route(AbstractWebService.BASE_URL, route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setContentType("text/html")
                    .setBody("<html><body><button style='display: none;'>Aceptar todo</button></body></html>"));
        });

        // Act & Assert: The method should catch the TimeoutException and not re-throw
        // it.
        try (Playwright testPlaywright = Playwright.create()) {
            assertDoesNotThrow(() -> {
                Page testPage = webService.createPage(testPlaywright);
                testPage.close();
            }, "The method should handle the timeout when the cookie button is not found/visible.");
        }
    }

    @Test
    void testExtractTextShouldReturnNotFoundForInvalidSelector() {
        // Arrange
        page.setContent("<div>Some content</div>");
        String invalidSelector = "invalid-selector-that-throws-exception::";

        // Act
        String text = webService.extractText(page, invalidSelector);

        // Assert
        assertEquals(AbstractWebService.NOT_FOUND, text);
    }

    @Test
    void testExtractTextWithLocatorShouldReturnNotFoundForInvalidSelector() {
        // Arrange
        page.setContent("<div id='container'><span>Some content</span></div>");
        Locator container = page.locator("#container");
        String invalidSelector = "invalid-selector-that-throws-exception::";

        // Act
        String text = webService.extractText(container, invalidSelector);

        // Assert
        assertEquals(AbstractWebService.NOT_FOUND, text);
    }
}
