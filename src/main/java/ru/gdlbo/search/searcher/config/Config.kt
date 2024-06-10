package ru.gdlbo.search.searcher.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "searcher")
data class Config(
    val path: String
)