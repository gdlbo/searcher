package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.FileTempInfo;
import ru.gdlbo.search.searcher.repository.PaginatedResult;
import ru.gdlbo.search.searcher.services.FileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FileModeration {
    @Autowired
    private FileService fileService;

    @GetMapping("review")
    public String review(@RequestParam(defaultValue = "0") int page,
                         @RequestParam Map<String, String> formData,
                         Authentication authentication,
                         Model model) {
        List<FileTempInfo> fileInfos = fileService.findAllTemp();
        model.addAttribute("fileInfos", fileInfos);
        return "review";
    }

    @PostMapping("/api/approve")
    public void approve(Long id) {
        fileService.approveTempFile(id);
    }

    @PostMapping("/api/refuse")
    public void decline(Long id) {
        fileService.removeTempFile(id);
    }

    @GetMapping("/api/getTempList")
    public List<FileTempInfo> getTempList() {
        return fileService.findAllTemp();
    }

    public List<Integer> getPageNumbers(int currentPage, int totalPages, int limit) {
        List<Integer> pageNumbers = new ArrayList<>();

        if (totalPages <= limit) {
            for (int i = 0; i < totalPages; i++) {
                pageNumbers.add(i);
            }
        } else {
            int startPage = Math.max(0, currentPage - limit / 2);
            int endPage = Math.min(startPage + limit, totalPages);

            if (endPage - startPage < limit) {
                startPage = Math.max(0, endPage - limit);
            }

            if (startPage > 0) {
                pageNumbers.add(-1);
            }

            for (int i = startPage; i < endPage; i++) {
                pageNumbers.add(i);
            }

            if (endPage < totalPages) {
                pageNumbers.add(totalPages);
            }
        }

        return pageNumbers;
    }
}