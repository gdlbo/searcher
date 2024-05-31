package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.services.FileService;

import java.util.List;

@Controller
public class FileModeration {
    @Autowired
    private FileService fileService;

    @PostMapping("/api/approve")
    public void approve(Long id) {
        fileService.approveTempFile(id);
    }

    @PostMapping("/api/refuse")
    public void decline(Long id) {
        fileService.removeTempFile(id);
    }

    @GetMapping("/api/getTempList")
    public List<FileInfo> getTempList() {
        return fileService.findAllTemp();
    }
}