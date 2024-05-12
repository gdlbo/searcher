package ru.gdlbo.search.searcher.repository;

import lombok.Getter;

@Getter
public class FileInfo {
    private final String filePath;
    private final String lastModified;

    public FileInfo(String filePath, String lastModified) {
        this.filePath = filePath;
        this.lastModified = lastModified;
    }
}