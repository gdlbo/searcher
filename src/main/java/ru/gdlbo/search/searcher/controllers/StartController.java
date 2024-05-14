package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.config.RestartManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Controller
public class StartController {
    @Autowired
    private RestartManager restartManager;

    @PostMapping("/api/submit")
    public String submitForm(@RequestParam("inputText") String inputText, @RequestParam(required = false, defaultValue = "false") String isDebug) throws IOException {
        Path propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties");
        Properties properties = new Properties();

        if (Files.exists(propertiesPath)) {
            try (FileInputStream inStream = new FileInputStream(propertiesPath.toFile())) {
                properties.load(inStream);
            }
        }

        properties.setProperty("searcher.path", inputText);
        properties.setProperty("searcher.isDebug", isDebug);

        try (FileOutputStream outStream = new FileOutputStream(propertiesPath.toFile())) {
            properties.store(outStream, null);
        }

        restartManager.restart();

        return "redirect:/search";
    }
}