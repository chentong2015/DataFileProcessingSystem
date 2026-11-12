package validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseFileValidator {

    protected boolean isValidFile(String fullpath, String type) {
        if (fullpath == null || fullpath.trim().isEmpty()) {
            return false;
        }

        Path path = Paths.get(fullpath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return false;
        }
        return isValidType(path, type);
    }

    protected boolean isValidType(Path path, String type) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return false;
        }

        String extension = fileName.substring(dotIndex + 1);
        return type.equalsIgnoreCase(extension);
    }
}
