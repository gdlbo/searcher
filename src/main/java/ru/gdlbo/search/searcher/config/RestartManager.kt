package ru.gdlbo.search.searcher.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import ru.gdlbo.search.searcher.SearcherApplication

@Component
class RestartManager : ApplicationListener<ApplicationReadyEvent> {
    private var context: ConfigurableApplicationContext? = null

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        this.context = event.applicationContext
    }

    fun restart() {
        val thread = Thread {
            context!!.close()
            context = SpringApplication.run(SearcherApplication::class.java)
        }
        thread.isDaemon = false
        thread.start()
    }
}