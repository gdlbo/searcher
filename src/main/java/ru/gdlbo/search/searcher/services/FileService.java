package ru.gdlbo.search.searcher.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.FileInfoRepository;
import ru.gdlbo.search.searcher.repository.PaginatedResult;

import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    @Autowired
    private FileInfoRepository fileInfoRepository;

    public List<FileInfo> findFiles(Specification<FileInfo> spec) {
        return fileInfoRepository.findAll(spec);
    }

    public PaginatedResult paginateFileInfos(List<FileInfo> fileInfos, int page) {
        int startIndex = page * 50;
        int endIndex = Math.min(startIndex + 50, fileInfos.size());
        int totalPages = (int) Math.ceil((double) fileInfos.size() / 50);

        List<FileInfo> paginatedFileInfos = fileInfos.subList(startIndex, endIndex);

        return new PaginatedResult(paginatedFileInfos, fileInfos.size() > endIndex, page, totalPages);
    }

    public void saveOrUpdateFile(FileInfo fileInfo) {
        fileInfoRepository.save(fileInfo);
    }

    public Optional<FileInfo> getFileById(Long id) {
        return fileInfoRepository.findById(id);
    }

    public boolean isAnyFilePresent() {
        return fileInfoRepository.count() > 0;
    }

    public boolean doesFileExist(Long id) {
        return fileInfoRepository.existsById(id);
    }


    public boolean existsByDecNumber(String decNumber) {
        return fileInfoRepository.existsByDecNumber(decNumber);
    }
}

