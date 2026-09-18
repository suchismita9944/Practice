package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and exposes values from src/test/resources/config.properties.
 * Uses a lazily-initialised singleton Properties instance so the file
 * is read from the classpath exactly once per JVM.
 */
public final class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;

    private ConfigReader() {
        // utility class - no instances
    }

    private static synchronized Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input == null) {
                    throw new RuntimeException("Unable to find " + CONFIG_FILE + " on the classpath");
                }
                properties.load(input);
                log.info("Loaded configuration from {}", CONFIG_FILE);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load " + CONFIG_FILE, e);
            }
        }
        return properties;
    }

    /** Returns the raw string value for a key, or null if absent. */
    public static String get(String key) {
        // Allow overriding any property via -Dkey=value on the Maven/CLI command line
        String systemOverride = System.getProperty(key);
        if (systemOverride != null && !systemOverride.isBlank()) {
            return systemOverride;
        }
        return getProperties().getProperty(key);
    }

    /** Returns the string value for a key, or a default if absent. */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return (value == null || value.isBlank()) ? defaultValue : Integer.parseInt(value.trim());
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return (value == null || value.isBlank()) ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    public static String baseUrl() {
        return get("baseUrl", "https://www.amazon.in/");
    }

    public static String browser() {
        return get("browser", "chrome");
    }

    public static boolean headless() {
        return getBoolean("headless", false);
    }

    public static int explicitWait() {
        return getInt("explicitWait", 15);
    }

    public static int implicitWait() {
        return getInt("implicitWait", 5);
    }

    public static int pageLoadTimeout() {
        return getInt("pageLoadTimeout", 30);
    }

    public static boolean maximizeWindow() {
        return getBoolean("maximizeWindow", true);
    }

    public static String excelDataFile() {
        return get("excelDataFile", "testdata/SearchTestData.xlsx");
    }
}
