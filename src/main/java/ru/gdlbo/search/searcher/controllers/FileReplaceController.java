package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.gdlbo.search.searcher.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class FileReplaceController {
    @Autowired
    private Config config;

    // This method is responsible for replacing a file with a new version
    @PostMapping("/api/replace")
    public String replaceFile(@RequestParam String filePath, @RequestParam MultipartFile file) throws Exception {
        System.out.println("Request received to replace file: " + filePath);

        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }

        // Securely check if the user has access to the file
        if (!filePath.contains(config.getPath())) {
            throw new IOException("Do not have access to this path");
        }

        File oldFile = new File(filePath);

        // Create a hidden directory to store old versions of the file
        File hiddenDir = new File(oldFile.getParentFile(), ".history");
        if (!hiddenDir.exists()) {
            if (!hiddenDir.mkdir()) {
                throw new Exception("Failed to create directory");
            }
        }

        // Move the old file to the hidden directory with a timestamp
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = dtf.format(now);
        String newFilePath = hiddenDir.getPath() + File.separator + oldFile.getName() + "_" + timestamp;
        Files.move(oldFile.toPath(), Paths.get(newFilePath));
        System.out.println("Moved old file to history: " + newFilePath);

        // Check if the hidden directory has more than 10 old versions of the file, and if so, delete the oldest one
        File[] files = hiddenDir.listFiles((dir, name) -> name.startsWith(oldFile.getName()));
        if (files != null && files.length > 10) {
            File oldestFile = files[0];
            for (int i = 1; i < files.length; i++) {
                if (files[i].lastModified() < oldestFile.lastModified()) {
                    oldestFile = files[i];
                }
            }

            if (!oldestFile.delete()) {
                throw new Exception("Failed to delete " + oldestFile.getName());
            }

            System.out.println("Deleted oldest file: " + oldestFile.getName());
        }

        // Replace the old file with the new one
        FileCopyUtils.copy(file.getInputStream().readAllBytes(), new File(filePath));
        System.out.println("Replaced file: " + filePath);

        return "redirect:/search";
    }
}