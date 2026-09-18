# Amazon.in Selenium + TestNG Automation Framework

A Page Object Model (POM) Selenium 4 test automation framework targeting
**https://www.amazon.in/**, built with Java 17, Selenium 4.45.x, TestNG 7.11.x,
and Maven. Browser driver binaries are managed automatically at runtime via
**WebDriverManager (Bonigarcia)** — there are no manual driver paths anywhere
in this project.

---

## Tech Stack

| Component        | Version   |
|-------------------|-----------|
| Java              | 17        |
| Selenium          | 4.45.0    |
| TestNG            | 7.11.0    |
| WebDriverManager  | 5.9.2     |
| Apache POI        | 5.3.0     |
| SLF4J + Logback   | 2.0.16 / 1.5.8 |
| Maven Compiler Plugin | 3.13.0 |
| Maven Surefire Plugin | 3.5.0  |

> **Version check:** Selenium's release cadence occasionally skips numbers.
> This project pins `4.45.0`, which was confirmed available on Maven Central
> at the time this framework was generated. If `mvn` reports it can't resolve
> that artifact, check https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
> for the closest available `4.45.x` (or newer) release and update the
> `<selenium.version>` property in `pom.xml` accordingly.

---

## Project Structure

```
amazon-selenium-framework/
├── pom.xml
├── README.md
├── .gitignore
├── src
│   ├── main/java
│   │   ├── pages/
│   │   │   ├── BasePage.java          # shared page object plumbing
│   │   │   ├── HomePage.java          # amazon.in landing page
│   │   │   ├── SearchResultsPage.java # /s?k=... results page
│   │   │   └── ProductPage.java       # product detail + add-to-cart
│   │   └── utils/
│   │       ├── ConfigReader.java      # reads config.properties (+ -D overrides)
│   │       ├── DriverManager.java     # ThreadLocal WebDriver factory (WebDriverManager-backed)
│   │       ├── WaitUtils.java         # WebDriverWait / ExpectedConditions helpers
│   │       └── ExcelUtils.java        # Apache POI reader for data-driven tests
│   └── test
│       ├── java
│       │   ├── base/BaseTest.java             # @BeforeMethod/@AfterMethod driver lifecycle
│       │   ├── listeners/TestListener.java    # TestNG ITestListener logging hooks
│       │   └── tests/
│       │       ├── AmazonSearchTest.java              # core + stretch (add-to-cart) scenarios
│       │       └── AmazonDataDrivenSearchTest.java    # @DataProvider + Excel-driven scenario
│       └── resources
│           ├── testng.xml
│           ├── config.properties
│           ├── logback.xml
│           └── testdata/SearchTestData.xlsx
```

---

## Prerequisites

- **JDK 17** installed and on your `PATH` (`java -version`)
- **Maven 3.9.x** installed (`mvn -version`)
- A Chrome, Firefox, or Edge browser installed locally (WebDriverManager
  downloads the matching *driver binary* automatically — it does not install
  the browser itself)
- Internet access on first run, so WebDriverManager can download the driver
  binary matching your installed browser version

---

## How to Build & Run

### 1. Build the project (compile + download dependencies)
```bash
mvn clean compile
```

### 2. Run the full test suite (via `testng.xml`, as wired into Surefire)
```bash
mvn test
```

### 3. Run against a specific browser
The `browser` value can be overridden without editing any file:
```bash
mvn test -Dbrowser=chrome
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
```

### 4. Run headless
```bash
mvn test -Dheadless=true
```

### 5. Run a single test class (bypassing testng.xml)
```bash
mvn test -Dtest=AmazonSearchTest
```

### 6. Run a different / custom TestNG suite file
```bash
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
```

All `config.properties` keys (`baseUrl`, `browser`, `headless`, `implicitWait`,
`explicitWait`, `pageLoadTimeout`, `maximizeWindow`, `excelDataFile`) can be
overridden the same way via `-Dkey=value`, since `ConfigReader` checks system
properties before falling back to the properties file.

---

## Test Scenarios Included

1. **`AmazonSearchTest.searchForProduct_ShouldDisplayResults`**
   Launches amazon.in → searches for `"laptop"` → asserts the results grid is
   populated and no "No results" banner is shown.

2. **`AmazonSearchTest.searchAndAddProductToCart_StretchScenario`**
   Searches for `"laptop"` → opens the first result → attempts to add it to
   the cart. This is treated as a **best-effort / soft assertion** rather
   than a hard failure — see *Known Limitations* below.

3. **`AmazonDataDrivenSearchTest.searchForProduct_DataDriven`**
   Reads keyword/expected-result pairs from
   `src/test/resources/testdata/SearchTestData.xlsx` via `ExcelUtils` and a
   TestNG `@DataProvider`, then repeats the search-and-verify flow once per
   row. Edit the spreadsheet to add more cases — no code changes required.

   | SearchKeyword                        | ExpectResults |
   |---------------------------------------|---------------|
   | laptop                                 | true          |
   | wireless mouse                         | true          |
   | bluetooth headphones                   | true          |
   | smartphone                             | true          |
   | qwqwqwqwqwzzznonexistentproduct123     | false         |

---

## Design Notes

- **No `Thread.sleep`** anywhere in the framework — all synchronization goes
  through `WaitUtils`, which wraps `WebDriverWait` + `ExpectedConditions`
  (visibility, clickability, presence, URL/title conditions, etc.).
- **Driver management is thread-safe** (`ThreadLocal<WebDriver>` in
  `DriverManager`), so the suite can be switched to parallel execution in
  `testng.xml` (`parallel="methods"` / `"classes"`) without rework.
- **Logging** goes through SLF4J + Logback to both the console and a rolling
  file under `logs/automation.log`, plus a `TestListener` that logs suite and
  test-level start/finish/pass/fail events.
- All config (base URL, browser, timeouts, Excel path) lives in
  `config.properties` and can be overridden per-run via `-Dkey=value`.

---

## Known Limitations

- **Amazon.in bot-detection / CAPTCHA:** Amazon.in actively runs automated-
  traffic detection and will intermittently present a "Continue shopping"
  interstitial or a CAPTCHA challenge, especially in headless mode, CI
  environments, or from datacenter IP ranges. `HomePage` includes a
  best-effort dismissal of the simple "Continue shopping" interstitial, but a
  full CAPTCHA challenge **cannot** be reliably solved by this (or any)
  automation framework. As a result:
  - The core search scenario is generally stable.
  - The **add-to-cart stretch scenario** is the most likely to be blocked
    intermittently and is intentionally implemented with a **soft
    assertion** (logged warning, not a hard `Assert` failure) so a CAPTCHA
    block doesn't fail the whole suite.
  - For consistent CI runs, consider running non-headless from a residential
    IP, adding delays between suite runs, or pointing the framework at a
    staging/mock e-commerce environment instead of production amazon.in.
- **Locators may drift over time:** Amazon.in frequently A/B tests its DOM
  structure and sponsored-content layout. If a test fails with a "no such
  element" / timeout error (rather than an assertion failure), re-inspect the
  current DOM and update the relevant `By` locator in the affected Page
  Object.

---

## Extending the Framework

- **New page:** add a class under `src/main/java/pages` extending `BasePage`.
- **New test:** add a class under `src/test/java/tests` extending `BaseTest`,
  then register it in `src/test/resources/testng.xml`.
- **New data-driven test:** add a method with a `@DataProvider` that calls
  `ExcelUtils.readSheetAsArray(...)` or `ExcelUtils.readSheet(...)`, backed by
  a new/updated `.xlsx` file under `src/test/resources/testdata/`.
- **Parallel execution:** set `parallel="methods"` (or `"classes"`) and a
  `thread-count` attribute on the `<suite>` element in `testng.xml` — the
  `ThreadLocal`-based `DriverManager` already supports this.
