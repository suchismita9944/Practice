package utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal Apache POI based reader for .xlsx test-data files.
 *
 * Convention: row 0 is the header row. Each subsequent row is returned as a
 * Map<String, String> keyed by header name, which keeps @DataProvider methods
 * decoupled from column ordering.
 */
public final class ExcelUtils {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);

    private ExcelUtils() {
        // utility class - no instances
    }

    /**
     * Reads all data rows from the first sheet of the given classpath-relative
     * Excel file (e.g. "testdata/SearchTestData.xlsx") into a list of row-maps.
     */
    public static List<Map<String, String>> readSheet(String classpathRelativePath) {
        return readSheet(classpathRelativePath, 0);
    }

    public static List<Map<String, String>> readSheet(String classpathRelativePath, int sheetIndex) {
        List<Map<String, String>> rows = new ArrayList<>();

        try (InputStream is = resolveInputStream(classpathRelativePath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalStateException("Excel file has no header row: " + classpathRelativePath);
            }

            int lastCol = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < lastCol; c++) {
                headers.add(getCellValueAsString(headerRow.getCell(c)));
            }

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    rowData.put(headers.get(c), getCellValueAsString(row.getCell(c)));
                }
                rows.add(rowData);
            }

            log.info("Read {} data row(s) from '{}'", rows.size(), classpathRelativePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + classpathRelativePath, e);
        }

        return rows;
    }

    /** Convenience: converts the sheet into a raw Object[][] for TestNG @DataProvider use. */
    public static Object[][] readSheetAsArray(String classpathRelativePath) {
        List<Map<String, String>> rows = readSheet(classpathRelativePath);
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }

    private static InputStream resolveInputStream(String classpathRelativePath) throws IOException {
        // First try classpath (works when running via Maven / packaged tests)
        InputStream classpathStream = ExcelUtils.class.getClassLoader().getResourceAsStream(classpathRelativePath);
        if (classpathStream != null) {
            return classpathStream;
        }
        // Fallback: try as a direct filesystem path under src/test/resources
        String fallbackPath = "src/test/resources/" + classpathRelativePath;
        log.warn("'{}' not found on classpath, falling back to file path '{}'", classpathRelativePath, fallbackPath);
        return new FileInputStream(fallbackPath);
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                // Print whole numbers without a trailing ".0"
                yield (value == Math.floor(value)) ? String.valueOf((long) value) : String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }
}
