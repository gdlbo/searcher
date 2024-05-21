package ru.gdlbo.search.searcher.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileHistory {
    private String lastModified;
    private String location;

    public FileHistory(String lastModified, String location) {
        this.lastModified = lastModified;
        this.location = location;
    }
}
