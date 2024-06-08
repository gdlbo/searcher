package ru.gdlbo.search.searcher.repository

import lombok.Getter
import ru.gdlbo.search.searcher.repository.dto.FileInfoDto

@Getter
class PaginatedResult(
    val paginatedFileInfos: List<FileInfoDto?>,
    val hasMoreResults: Boolean,
    val page: Int,
    val totalPages: Int
) 