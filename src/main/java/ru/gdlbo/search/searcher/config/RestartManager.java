package ru.gdlbo.search.searcher.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import ru.gdlbo.search.searcher.SearcherApplication;

@Component
public class RestartManager implements ApplicationListener<ApplicationReadyEvent> {
    private ConfigurableApplicationContext context;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.context = event.getApplicationContext();
    }

    public void restart() {
        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(SearcherApplication.class);
        });
        thread.setDaemon(false);
        thread.start();
    }
}