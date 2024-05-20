package ru.gdlbo.search.searcher.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.FileRepository;
import ru.gdlbo.search.searcher.repository.PaginatedResult;

import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    public List<FileInfo> findFiles(Specification<FileInfo> spec) {
        return fileRepository.findAll(spec);
    }

    public PaginatedResult paginateFileInfos(List<FileInfo> fileInfos, int page) {
        int startIndex = page * 50;
        int endIndex = Math.min(startIndex + 50, fileInfos.size());
        int totalPages = (int) Math.ceil((double) fileInfos.size() / 50);

        List<FileInfo> paginatedFileInfos = fileInfos.subList(startIndex, endIndex);

        return new PaginatedResult(paginatedFileInfos, fileInfos.size() > endIndex, page, totalPages);
    }

    public void saveOrUpdateFile(FileInfo fileInfo) {
        fileRepository.save(fileInfo);
    }

    public Optional<FileInfo> getFileById(Long id) {
        return fileRepository.findById(id);
    }

    public boolean hasRecords() {
        return fileRepository.existsById(1L);
    }
}

