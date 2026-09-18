package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.SearchResultsPage;
import utils.ConfigReader;
import utils.ExcelUtils;

import java.util.Map;

/**
 * Data-driven variant of the search scenario. Search keywords and the
 * expected "results present" flag are read from an Excel workbook
 * (src/test/resources/testdata/SearchTestData.xlsx) via ExcelUtils, and fed
 * into the test through a TestNG @DataProvider.
 */
public class AmazonDataDrivenSearchTest extends BaseTest {

    @DataProvider(name = "searchKeywordsFromExcel")
    public Object[][] searchKeywordsFromExcel() {
        return ExcelUtils.readSheetAsArray(ConfigReader.excelDataFile());
    }

    @Test(dataProvider = "searchKeywordsFromExcel",
            description = "Data-driven: search Amazon.in for each keyword row in SearchTestData.xlsx")
    public void searchForProduct_DataDriven(Map<String, String> row) {
        String keyword = row.get("SearchKeyword");
        boolean expectResults = Boolean.parseBoolean(row.getOrDefault("ExpectResults", "true"));

        log.info("Data-driven case -> keyword='{}', expectResults={}", keyword, expectResults);

        homePage.open();
        SearchResultsPage resultsPage = homePage.searchFor(keyword);

        boolean resultsShown = resultsPage.areResultsDisplayed();

        if (expectResults) {
            Assert.assertTrue(resultsShown, "Expected results for keyword '" + keyword + "' but none were displayed");
        } else {
            Assert.assertFalse(resultsShown, "Expected NO results for keyword '" + keyword + "' but results were displayed");
        }
    }
}
