package ru.gdlbo.search.searcher.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FileHistoryController {
    // This method is responsible for retrieving the history of a file
    @GetMapping("/api/history")
    public ResponseEntity<List<FileInfo>> getFileHistory(@RequestParam String fileName) {
        System.out.println("Request received to get history for file: " + fileName);

        File file = new File(fileName);
        File parentDir = file.getParentFile();
        File hiddenDir = new File(parentDir, ".history");

        // Get all files in the hidden directory with the given file name
        File[] files = hiddenDir.listFiles((dir, name) -> name.startsWith(file.getName()));

        // If there are no files, return an empty list
        if (files == null) {
            System.out.println("No history found for file: " + fileName);
            files = new File[0];
        }

        // Sort the files by timestamp
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        fileList.sort(Comparator.comparingLong(File::lastModified));

        List<FileInfo> fileHistory = fileList.stream()
                .map(f -> new FileInfo(f.getAbsolutePath(), String.valueOf(f.lastModified()), null))
                .collect(Collectors.toList());

        System.out.println("Returning history for file: " + fileName);

        return ResponseEntity.ok(fileHistory);
    }

    // This method is responsible for removing a file from the history
    @GetMapping("/api/remove")
    public String removeFileFromHistory(@RequestParam String filePath, Authentication authentication) {
        System.out.println("Request received to remove file: " + filePath);

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