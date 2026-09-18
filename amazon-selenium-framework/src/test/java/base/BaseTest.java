package base;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.HomePage;
import utils.DriverManager;

/**
 * Base class for all TestNG test classes: owns WebDriver lifecycle
 * (create before each test method, quit after) so individual test
 * classes stay focused on scenario logic only.
 */
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected WebDriver driver;
    protected HomePage homePage;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log.info("===== Test setup: initializing WebDriver =====");
        DriverManager.initDriver();
        driver = DriverManager.getDriver();
        homePage = new HomePage(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            log.error("Test FAILED: {}", result.getName(), result.getThrowable());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            log.info("Test PASSED: {}", result.getName());
        }
        log.info("===== Test teardown: quitting WebDriver =====");
        DriverManager.quitDriver();
    }
}
