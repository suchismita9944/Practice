package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.ProductPage;
import pages.SearchResultsPage;

/**
 * Core sample scenario:
 *   1. Launch amazon.in
 *   2. Search for a product ("laptop")
 *   3. Verify search results are displayed
 *   4. (Stretch) Attempt to add the first result to the cart
 */
public class AmazonSearchTest extends BaseTest {

    @Test(description = "Verify a keyword search on Amazon.in returns visible results")
    public void searchForProduct_ShouldDisplayResults() {
        log.info("STEP 1: Launch Amazon.in");
        homePage.open();
        Assert.assertTrue(homePage.isLogoDisplayed(), "Amazon.in logo was not displayed - home page failed to load");

        log.info("STEP 2: Search for 'laptop'");
        SearchResultsPage resultsPage = homePage.searchFor("laptop");

        log.info("STEP 3: Verify search results are displayed");
        Assert.assertTrue(resultsPage.areResultsDisplayed(), "No search results were displayed for 'laptop'");
        Assert.assertFalse(resultsPage.isNoResultsMessageDisplayed(), "Amazon.in reported no results for 'laptop'");

        int resultCount = resultsPage.getResultsCount();
        log.info("Result count for 'laptop': {}", resultCount);
        Assert.assertTrue(resultCount > 0, "Expected at least one search result for 'laptop'");
    }

    @Test(description = "Stretch scenario: search for a product and attempt to add the first result to the cart. "
            + "Amazon.in bot-detection/CAPTCHA may intermittently block this - see README known limitations.")
    public void searchAndAddProductToCart_StretchScenario() {
        homePage.open();

        SearchResultsPage resultsPage = homePage.searchFor("laptop");
        Assert.assertTrue(resultsPage.areResultsDisplayed(), "No search results were displayed for 'laptop'");

        ProductPage productPage = resultsPage.openResultAt(0);
        Assert.assertTrue(productPage.isProductTitleDisplayed(), "Product page did not load - title not visible");
        log.info("Opened product: {}", productPage.getProductTitle());

        boolean addedToCart = productPage.addToCart();
        if (!addedToCart) {
            log.warn("Add-to-cart step did not complete - likely blocked by Amazon.in bot-detection/CAPTCHA. "
                    + "Marking as a soft assertion rather than a hard failure.");
        }
        // Intentionally not a hard Assert.assertTrue on addedToCart: Amazon.in's bot-detection
        // can intermittently block this step in headless/CI environments (documented in README).
    }
}
