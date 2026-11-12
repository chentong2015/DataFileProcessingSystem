package validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XmlFileValidator extends BaseFileValidator {

    private static final int HEADER_LENGTH = 256;
    private static final String WC_XSD = "world-check.xsd";
    private static final String WC_PREMIUM_XSD = "premium-world-check.xsd";

    public boolean validateFile(String fullpath) {
        if (isValidFile(fullpath, "xml")) {
            Path xmlPath = Paths.get(fullpath);
            return isValidContent(xmlPath);
        }
        return false;
    }

    private boolean isValidContent(Path xmlPath) {
        byte[] header = new byte[HEADER_LENGTH];
        try (InputStream input = Files.newInputStream(xmlPath)) {
            int length = input.read(header);
            if (length <= 0) {
                return false;
            }

            String content = new String(header, 0, length, StandardCharsets.UTF_8);
            return content.contains(WC_XSD) || content.contains(WC_PREMIUM_XSD);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
