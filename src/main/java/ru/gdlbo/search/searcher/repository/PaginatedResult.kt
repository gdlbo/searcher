package ru.gdlbo.search.searcher.repository

import lombok.Getter

@Getter
class PaginatedResult(
    val paginatedFileInfos: List<FileInfo?>,
    val hasMoreResults: Boolean,
    val page: Int,
    val totalPages: Int
) 