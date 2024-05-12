package ru.gdlbo.search.searcher.config;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class ConfigGenerator {
    public void generateConfigFile(String content) {
        try {
            File file = new File("application.properties");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }
}
