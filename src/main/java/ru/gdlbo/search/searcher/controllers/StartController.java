package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.config.RestartManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Controller
public class StartController {
    @Autowired
    private RestartManager restartManager;

    @GetMapping("/start")
    public String getStartPage() {
        return "start";
    }

    @GetMapping("/api/submit")
    public ResponseEntity<String> submitForm(@RequestParam(required = false) String searcherPath, @RequestParam(required = false, defaultValue = "false") String isDebug) throws IOException {
        Path propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties");

        if (!propertiesPath.toFile().exists()) {
            propertiesPath.toFile().createNewFile();
        }

        Properties properties = new Properties();

        try (FileInputStream inStream = new FileInputStream(propertiesPath.toFile())) {
            properties.load(inStream);
        }

        if (searcherPath == null || searcherPath.isEmpty()) {
            searcherPath = properties.getProperty("searcher.path");
        }

        if (isDebug == null || isDebug.isEmpty()) {
            isDebug = properties.getProperty("searcher.isDebug");
        }

        properties.setProperty("searcher.path", searcherPath);
        properties.setProperty("searcher.isDebug", isDebug);

        try (FileOutputStream outStream = new FileOutputStream(propertiesPath.toFile())) {
            properties.store(outStream, null);
        }

        restartManager.restart();

        return ResponseEntity.ok("Restarting...");
    }
}