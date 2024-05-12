package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.gdlbo.search.searcher.config.Config;

import java.io.File;
import java.io.IOException;

@Controller
public class FileUploadController {
    @Autowired
    private Config config;

    @PostMapping("/api/upload")
    public String uploadFile(@RequestParam String pathToUpload, @RequestParam MultipartFile file) throws IOException {
        System.out.println("Request received to upload file: " + file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        // Securely check if the user has access to the path
        if (!pathToUpload.contains(config.getPath())) {
            throw new IOException("Do not have access to this path");
        }

        String filePath = pathToUpload + File.separator + file.getOriginalFilename();

        File newFile = new File(filePath);
        if (!newFile.getParentFile().exists()) {
            if (!newFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory");
            }
        }

        file.transferTo(newFile);

        System.out.println("File uploaded successfully: " + file.getOriginalFilename());

        // Redirect to the search page
        return "redirect:/search";
    }
}