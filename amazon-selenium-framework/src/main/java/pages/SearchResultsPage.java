package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object for the Amazon.in search results ("/s?k=...") page.
 */
public class SearchResultsPage extends BasePage {

    private final By resultsContainer = By.cssSelector("div.s-main-slot");
    private final By resultItems = By.cssSelector("div.s-main-slot div[data-component-type='s-search-result']");
    private final By resultTitles = By.cssSelector("div.s-main-slot div[data-component-type='s-search-result'] h2 span");
    private final By noResultsBanner = By.xpath("//*[contains(text(),'No results for')]");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean areResultsDisplayed() {
        try {
            waitUtils.waitForVisibility(resultsContainer);
            List<WebElement> items = waitUtils.waitForAllVisible(resultItems);
            log.info("Search results displayed: {} item(s) found", items.size());
            return !items.isEmpty();
        } catch (Exception e) {
            log.error("Search results were not displayed within the timeout", e);
            return false;
        }
    }

    public int getResultsCount() {
        return driver.findElements(resultItems).size();
    }

    public List<String> getResultTitles() {
        return waitUtils.waitForAllVisible(resultTitles).stream()
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .toList();
    }

    public boolean isNoResultsMessageDisplayed() {
        return !driver.findElements(noResultsBanner).isEmpty();
    }

    /** Opens the Nth (0-indexed) product from the results in the same tab and returns its Product Page. */
    public ProductPage openResultAt(int index) {
        List<WebElement> items = waitUtils.waitForAllVisible(resultItems);
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException("Requested result index " + index + " but only " + items.size() + " results present");
        }
        WebElement item = items.get(index);
        WebElement titleLink = item.findElement(By.cssSelector("h2 a"));
        log.info("Opening search result at index {}: '{}'", index, titleLink.getText());
        titleLink.click();
        return new ProductPage(driver);
    }
}
