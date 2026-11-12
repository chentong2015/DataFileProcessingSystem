package validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CsvFileValidator extends BaseFileValidator {

    private static final int NEED_VALID_LINES = 3;

    public boolean validateFile(String fullpath) {
        if (isValidFile(fullpath, "csv")) {
            Path csvPath = Paths.get(fullpath);
            return isValidContent(csvPath);
        }
        return false;
    }

    private boolean isValidContent(Path csvPath) {
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            int validLines = 0;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\"")) {
                   line = line.replace("\"", "");
                }

                String[] columns = line.split(",", -1);
                if (columns.length != 3) {
                    return false;
                }
                if (columns[0].trim().isEmpty()) {
                    return false;
                }
                if (!columns[2].trim().matches("[A-Fa-f0-9]{64}")) {
                    return false;
                }

                validLines++;
                if (validLines > NEED_VALID_LINES) {
                    break;
                }
            }
            return true;
        } catch (IOException | SecurityException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}