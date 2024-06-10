package ru.gdlbo.search.searcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.gdlbo.search.searcher.config.Config

@SpringBootApplication
@EnableConfigurationProperties(Config::class)
open class SearcherApplication

fun main(args: Array<String>) {
    runApplication<SearcherApplication>(*args)
}