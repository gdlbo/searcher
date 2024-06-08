package ru.gdlbo.search.searcher.config

import lombok.Getter
import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "searcher")
data class Config(
    val path: String
)