package ru.gdlbo.search.searcher.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class FileHistoryController {
    private static File[] getFiles(String path) {
        File file = new File(path);
        File parentDir = file.getParentFile();
        File hiddenDir = new File(parentDir, ".history");

        return hiddenDir.listFiles((dir, name) -> {
            String baseName = file.getName();
            int lastDotIndex = baseName.lastIndexOf('.');
            if (lastDotIndex != -1) {
                baseName = baseName.substring(0, lastDotIndex);
            }

            String currentFileNameWithoutExtension = name;
            int currentFileLastDotIndex = currentFileNameWithoutExtension.lastIndexOf('.');
            if (currentFileLastDotIndex != -1) {
                currentFileNameWithoutExtension = currentFileNameWithoutExtension.substring(0, currentFileLastDotIndex);
            }

            return currentFileNameWithoutExtension.startsWith(baseName);
        });
    }

    private static String extractDateFromFileName(File file) {
        String fileName = file.getName();
        String datePattern = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}";

        Pattern pattern = Pattern.compile(datePattern);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(0).replace("_", " ");
        } else {
            return "N/A";
        }
    }

    // This method is responsible for retrieving the history of a file
    @GetMapping("/api/history")
    public ResponseEntity<List<FileHistory>> getFileHistory(@RequestParam String fileName) {
        System.out.println("Request received to get history for file: " + fileName);

        File file = new File(fileName);
        File[] files = getFiles(fileName);

        // If there are no files, return an empty list
        if (files == null) {
            System.out.println("No history found for file: " + fileName);
            files = new File[0];
        }

        // Sort the files by timestamp
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        fileList.sort(Comparator.comparingLong(File::lastModified));
        String location = file.getAbsolutePath();

        List<FileHistory> fileHistory = fileList.stream()
                .map(f -> new FileHistory(extractDateFromFileName(f), location))
                .toList();

        System.out.println("Returning history for file: " + fileName);

        return ResponseEntity.ok(fileHistory);
    }

    @DeleteMapping("/api/removeAllFromHistory")
    public ResponseEntity<Void> deleteFileHistory(@RequestParam String fileName) {
        System.out.println("Request received to delete all history for file: " + fileName);

        File[] files = getFiles(fileName);

        if (files != null) {
            for (File historyFile : files) {
                if (historyFile.delete()) {
                    System.out.println("Deleted history file: " + historyFile.getName());
                } else {
                    System.out.println("Failed to delete history file: " + historyFile.getName());
                }
            }
        } else {
            System.out.println("No history found for file: " + fileName);
        }

        return ResponseEntity.noContent().build();
    }

    // This method is responsible for removing a file from the history
    @GetMapping("/api/removeFromHistory")
    public String removeFileFromHistory(@RequestParam String filePath, Authentication authentication) {
        System.out.println("Request received to remove file history: " + filePath);

        File fileToRemove = new File(filePath);

        // Check to not abuse deletion of files
        if (!fileToRemove.getAbsolutePath().contains(".history") && !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            System.out.println("Attempt to delete file outside of history directory: " + filePath);
            return "redirect:/error";
        }

        // Remove the file from the hidden directory
        if (!fileToRemove.delete()) {
            System.out.println("Failed to delete file: " + filePath);
            return "redirect:/error";
        }

        System.out.println("Deleted file: " + filePath);

        return "redirect:/search";
    }
}