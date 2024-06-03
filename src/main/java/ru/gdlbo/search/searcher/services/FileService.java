package ru.gdlbo.search.searcher.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import ru.gdlbo.search.searcher.config.Config;
import ru.gdlbo.search.searcher.repository.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private FileTempInfoRepository tempInfoRepository;

    @Autowired
    private Config config;
    @Autowired
    private FileTempInfoRepository fileTempInfoRepository;

    public List<FileTempInfo> findAllTemp() {
        return tempInfoRepository.findAll();
    }

    public List<FileTempInfo> findByUser(User user) {
        return tempInfoRepository.findByUser(user);
    }

    public void removeTempFile(Long id) {
        processTempFile(id, true);
    }

    public void approveTempFile(Long id) {
        processTempFile(id, false);
    }

    private void processTempFile(Long id, boolean isRemoval) {
        tempInfoRepository.findById(id).ifPresent(fileInfo -> {
            File tempFile = new File(fileInfo.getLocation());

            if (!tempFile.exists()) {
                throw new IllegalStateException("File " + fileInfo.getLocation() + " does not exist");
            }

            if (!isRemoval) {
                String newPath = config.getPath() + "/" + tempFile.getName();

                try {
                    FileCopyUtils.copy(tempFile, new File(newPath));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to copy file " + tempFile.getAbsolutePath(), e);
                }

                fileInfo.setLocation(newPath);

                fileInfoRepository.save(new FileInfo(fileInfo));
            }

            if (!tempFile.delete()) {
                throw new IllegalStateException("Failed to delete file " + fileInfo.getLocation());
            }

            tempInfoRepository.delete(fileInfo);
        });
    }

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

    public void saveTempFile(FileTempInfo fileInfo) {
        tempInfoRepository.save(fileInfo);
    }

    public Optional<FileInfo> getFileById(Long id) {
        return fileInfoRepository.findById(id);
    }

    public Optional<FileTempInfo> getTempFileById(Long id) {
        return fileTempInfoRepository.findById(id);
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