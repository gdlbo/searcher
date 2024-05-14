package ru.gdlbo.search.searcher.repository;

import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedResult {
    private final List<FileInfo> paginatedFileInfos;
    private final boolean hasMoreResults;
    private final int page;
    private final int totalPages;

    public PaginatedResult(List<FileInfo> paginatedFileInfos, boolean hasMoreResults, int page, int totalPages) {
        this.paginatedFileInfos = paginatedFileInfos;
        this.hasMoreResults = hasMoreResults;
        this.page = page;
        this.totalPages = totalPages;
    }
}