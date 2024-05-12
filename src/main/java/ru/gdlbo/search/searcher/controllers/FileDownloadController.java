package ru.gdlbo.search.searcher.controllers;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class FileDownloadController {
    // This method is responsible for handling file download requests
    @GetMapping("/api/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam String filePath) throws IOException {

        // Creating a new File object from the provided filePath
        File file = new File(filePath);
        System.out.println("File object created with path: " + filePath);

        // Creating an InputStreamResource to read from the file
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        System.out.println("InputStreamResource created to read from the file");

        // Getting the name of the file
        String fileName = file.getName();
        System.out.println("File name: " + fileName);

        // Encoding the file name to ensure it's properly displayed in all browsers
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        System.out.println("Encoded file name: " + encodedFileName);

        // Building and returning the ResponseEntity with the file as the body
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}