package ru.gdlbo.search.searcher.repository

import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
class FileHistory(private val lastModified: String, private val location: String)
