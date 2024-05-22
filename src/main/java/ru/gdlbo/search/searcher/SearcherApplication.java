package ru.gdlbo.search.searcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.gdlbo.search.searcher.config.Config;

@SpringBootApplication
@EnableConfigurationProperties(Config.class)
public class SearcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearcherApplication.class, args);
    }
}