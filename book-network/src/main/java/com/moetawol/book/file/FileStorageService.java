package com.moetawol.book.file;

import com.moetawol.book.book.Book;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Service // Marks this class as a Spring service bean (so it can be auto-injected)
@Slf4j // Lombok annotation to enable logging (log.info(), log.error(), etc.)
@RequiredArgsConstructor // Lombok generates constructor for final fields (none here, but keeps class ready)
public class FileStorageService {

    // Reads a value from application.properties or application.yml
    @Value("${application.file.uploads.photos-output-path}")
    private String fileUploadPath;

    /**
     * Public method to save a file related to a book and a specific user.
     * It builds a folder structure like "users/{userId}" inside the base upload path.
     */
    public String saveFile(
            @Nonnull MultipartFile sourceFile, // Uploaded file
            @Nonnull Book book,                // The book it's related to (not used here but may be for naming/tagging)
            @Nonnull UUID userId               // ID of the user to organize the files
    ) {
        // Define a sub_directory for the specific user
        final String fileUploadSubPath = "users" + separator + userId;
        // Call the method to actually handle the upload and return the path
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    /**
     * Handles writing the file to disk.
     * Creates directories if needed, writes the file, returns the saved path.
     */
    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadSubPath
    ) {
        // Construct the final path: base path + /users/{userId}
        final String finalUploadPath = fileUploadPath + separator + fileUploadSubPath;

        // Create a File object representing the directory
        File targetFolder = new File(finalUploadPath);

        // If the directory doesn't exist, create it (mkdirs = creates parent directories too)
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs(); // create directory
            if (!folderCreated) {
                log.warn("Failed to create the target folder: {}", targetFolder);
                return null; // Stop execution if folder creation fails
            }
        }

        // Get the original file extension (.jpg, .png, etc.)
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());

        // Create a new unique filename based on timestamp + extension
        String targetFilePath = finalUploadPath + separator + currentTimeMillis() + "." + fileExtension;

        // Convert it to a Path object
        Path targetPath = Paths.get(targetFilePath);

        try {
            // Write the uploaded file’s bytes to the target location
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File saved to: {}", targetFilePath); // Log success
            return targetFilePath;
        } catch (IOException e) {
            // Log if writing failed
            log.error("File was not saved", e);
        }

        return null; // Return null if saving failed
    }

    /**
     * Helper method to extract file extension from the file name.
     * Example: file.jpg → jpg
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ""; // No dot found, no extension
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase(); // Get substring after dot
    }
}
