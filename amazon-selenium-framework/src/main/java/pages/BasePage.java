package pages;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.WaitUtils;

/**
 * Common base class for all Page Objects: holds the WebDriver reference
 * and a shared WaitUtils instance so subclasses never need to instantiate
 * WebDriverWait themselves.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriver driver;
    protected final WaitUtils waitUtils;
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getTitle() {
        return driver.getTitle();
    }
}
