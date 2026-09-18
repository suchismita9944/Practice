package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

/**
 * Page Object for the Amazon.in landing / home page.
 *
 * NOTE: Amazon.in's DOM/attributes can change without notice and the site
 * actively runs bot-detection. Locators below are based on stable-looking
 * IDs as of this writing; re-verify them if tests start failing due to
 * "element not found" rather than assertion failures.
 */
public class HomePage extends BasePage {

    private final By searchBox = By.id("twotabsearchtextbox");
    private final By searchSubmitButton = By.id("nav-search-submit-button");
    private final By navLogo = By.id("nav-logo-sprites");
    private final By cartCount = By.id("nav-cart-count");
    private final By continueShoppingButton = By.xpath("//button[contains(., 'Continue shopping')]");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /** Navigates to the base URL configured in config.properties. */
    public HomePage open() {
        log.info("Navigating to {}", ConfigReader.baseUrl());
        driver.get(ConfigReader.baseUrl());
        dismissContinueShoppingInterstitialIfPresent();
        waitUtils.waitForVisibility(searchBox);
        return this;
    }

    /**
     * Amazon.in occasionally shows a "Continue shopping" bot-check interstitial
     * for automated / headless traffic. This best-effort check clears it when
     * present so the rest of the flow is not blocked; if it is not present the
     * method simply returns.
     */
    private void dismissContinueShoppingInterstitialIfPresent() {
        try {
            WebElement continueBtn = new utils.WaitUtils(driver, 3).waitFor(
                    ExpectedConditions.elementToBeClickable(continueShoppingButton));
            log.warn("Bot-check interstitial detected - clicking 'Continue shopping'");
            continueBtn.click();
        } catch (Exception ignored) {
            // Interstitial not shown - nothing to do
        }
    }

    public boolean isLogoDisplayed() {
        return waitUtils.waitForVisibility(navLogo).isDisplayed();
    }

    public SearchResultsPage searchFor(String productKeyword) {
        log.info("Searching for product: '{}'", productKeyword);
        WebElement box = waitUtils.waitForVisibility(searchBox);
        box.clear();
        box.sendKeys(productKeyword);
        waitUtils.waitForClickable(searchSubmitButton).click();
        return new SearchResultsPage(driver);
    }

    public int getCartCount() {
        try {
            String text = waitUtils.waitForVisibility(cartCount).getText().trim();
            return text.isEmpty() ? 0 : Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
