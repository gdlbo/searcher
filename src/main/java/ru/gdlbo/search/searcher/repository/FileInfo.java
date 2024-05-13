package ru.gdlbo.search.searcher.repository;

import lombok.Getter;

@Getter
public class FileInfo {
    private final String filePath;
    private final String lastModified;
    private final String creationTime;

    public FileInfo(String filePath, String lastModified, String creationTime) {
        this.filePath = filePath;
        this.lastModified = lastModified;
        this.creationTime = creationTime;
    }
}