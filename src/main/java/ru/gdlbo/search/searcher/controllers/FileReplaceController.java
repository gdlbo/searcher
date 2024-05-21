package ru.gdlbo.search.searcher.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class FileReplaceController {
    @PostMapping("/api/replace")
    public String replaceFile(@RequestParam String filePath, @RequestParam MultipartFile file) throws Exception {
        System.out.println("Request received to replace file: " + filePath);

        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }

        File oldFile = new File(filePath);

        File hiddenDir = createHiddenDirectory(oldFile);
        String newFilePath = moveOldFileToHistory(oldFile, hiddenDir);
        checkAndDeleteOldVersions(hiddenDir, oldFile.getName());

        replaceOldFileWithNew(file, filePath);

        return "redirect:/search";
    }

    private File createHiddenDirectory(File oldFile) throws Exception {
        File hiddenDir = new File(oldFile.getParentFile(), ".history");
        if (!hiddenDir.exists()) {
            if (!hiddenDir.mkdir()) {
                throw new Exception("Failed to create directory");
            }
        }
        return hiddenDir;
    }

    private String moveOldFileToHistory(File oldFile, File hiddenDir) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = dtf.format(now);

        String oldFileName = oldFile.getName();
        int dotIndex = oldFileName.lastIndexOf('.');
        String nameWithoutExtension = (dotIndex == -1) ? oldFileName : oldFileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : oldFileName.substring(dotIndex);

        String newFilePath = hiddenDir.getPath() + File.separator + nameWithoutExtension + "_" + timestamp + extension;
        Files.move(oldFile.toPath(), Paths.get(newFilePath));
        System.out.println("Moved old file to history: " + newFilePath);

        return newFilePath;
    }

    private void checkAndDeleteOldVersions(File hiddenDir, String oldFileName) throws Exception {
        File[] files = hiddenDir.listFiles((dir, name) -> name.startsWith(oldFileName.substring(0, oldFileName.lastIndexOf('.'))));
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
    }

    private void replaceOldFileWithNew(MultipartFile file, String filePath) throws IOException {
        FileCopyUtils.copy(file.getInputStream().readAllBytes(), new File(filePath));
        System.out.println("Replaced file: " + filePath);
    }
}