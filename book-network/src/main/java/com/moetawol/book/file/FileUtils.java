package com.moetawol.book.file;

// Import for logging
import lombok.extern.slf4j.Slf4j;

// Import for string utility methods
import org.apache.commons.lang3.StringUtils;

// Java standard classes for working with files
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Lombok annotation to enable SLF4J logging
@Slf4j
public class FileUtils {

    // Static utility method to read a file from a given path and return its bytes
    public static byte[] readFileFromLocation(String fileUrl) {

        // Check if the given path is null, empty, or only whitespace
        if (StringUtils.isBlank(fileUrl)) {
            return null; // Don't proceed if the path is invalid
        }

        try {
            // Convert the string path to a File object, then get its Path representation
            Path filePath = new File(fileUrl).toPath();

            // Read the entire file content as a byte array and return it
            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            // Log a warning if the file couldn't be read (maybe it doesn't exist)
            log.warn("No file found in the path {}", fileUrl);
        }

        // If reading the file fails or the path is invalid, return null
        return null;
    }
}
