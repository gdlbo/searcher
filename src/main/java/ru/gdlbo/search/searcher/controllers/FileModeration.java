package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileTempInfo;
import ru.gdlbo.search.searcher.services.FileService;

import java.util.List;

@Controller
public class FileModeration {
    @Autowired
    private FileService fileService;

    @GetMapping("review")
    public String review(Model model) {
        List<FileTempInfo> fileInfos = fileService.findAllTemp();
        model.addAttribute("fileInfos", fileInfos);
        return "review";
    }

    @GetMapping("/api/approve")
    public void approve(@RequestParam Long id) {
        fileService.approveTempFile(id);
    }

    @GetMapping("/api/refuse")
    public void decline(@RequestParam Long id) {
        fileService.removeTempFile(id);
    }

    @GetMapping("/api/getTempList")
    public List<FileTempInfo> getTempList() {
        return fileService.findAllTemp();
    }
}