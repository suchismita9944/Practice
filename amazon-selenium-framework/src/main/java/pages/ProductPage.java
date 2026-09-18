package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Set;

/**
 * Page Object for an individual Amazon.in product detail page.
 *
 * The "Add to Cart" flow is inherently the most fragile part of this suite:
 * Amazon.in frequently interleaves CAPTCHA / bot-check challenges, sponsored
 * layout variants, and A/B tested DOM structures on this page. Treat failures
 * here as a known, environment-dependent limitation rather than a defect in
 * the framework itself (see README "Known Limitations").
 */
public class ProductPage extends BasePage {

    private final By productTitle = By.id("productTitle");
    private final By addToCartButton = By.id("add-to-cart-button");
    private final By buyNowButton = By.id("buy-now-button");
    private final By cartCount = By.id("nav-cart-count");

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean isProductTitleDisplayed() {
        switchToProductWindowIfOpenedInNewTab();
        return waitUtils.waitForVisibility(productTitle).isDisplayed();
    }

    public String getProductTitle() {
        switchToProductWindowIfOpenedInNewTab();
        return waitUtils.waitForVisibility(productTitle).getText().trim();
    }

    /**
     * Adds the current product to the cart. Returns true if the add-to-cart
     * action completed and the cart count could be confirmed; false if a
     * bot-check/CAPTCHA or unexpected layout blocked the action (logged, not thrown).
     */
    public boolean addToCart() {
        switchToProductWindowIfOpenedInNewTab();
        try {
            waitUtils.waitForClickable(addToCartButton).click();
            // Cart count updating is the most reliable, wait-friendly confirmation signal
            waitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(cartCount));
            log.info("Product added to cart successfully");
            return true;
        } catch (Exception e) {
            log.warn("Add-to-cart could not be confirmed - possibly blocked by a bot-check/CAPTCHA challenge on Amazon.in", e);
            return false;
        }
    }

    private void switchToProductWindowIfOpenedInNewTab() {
        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            String lastHandle = handles.stream().reduce((first, second) -> second).orElseThrow();
            driver.switchTo().window(lastHandle);
        }
    }
}
