package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.FileNameParser;
import ru.gdlbo.search.searcher.config.Config;
import ru.gdlbo.search.searcher.repository.*;
import ru.gdlbo.search.searcher.services.FileService;
import ru.gdlbo.search.searcher.services.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class FileSearchController {
    @Autowired
    private Config config;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public String searchFiles(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false, defaultValue = "false") Boolean showHidden,
                              @RequestParam(required = false) String decNumber,
                              @RequestParam(required = false) String deviceName,
                              @RequestParam(required = false) String documentType,
                              @RequestParam(required = false) String usedDevices,
                              @RequestParam(required = false) String project,
                              @RequestParam(required = false) String inventoryNumber,
                              @RequestParam(required = false) String lastModified,
                              @RequestParam(required = false) String location,
                              @RequestParam(required = false) String creationTime,
                              Authentication authentication,
                              Model model) {

        String searchPath = config.getPath();
        if (searchPath == null || searchPath.isEmpty()) {
            return "redirect:/start";
        }

        if (!fileService.hasRecords()) {
            createDummyFiles(100);
        }

        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/error";
        }

        Specification<FileInfo> spec = FileInfoSpecification.createSpecification(
                decNumber, deviceName, documentType, usedDevices, project,
                inventoryNumber, lastModified, location, creationTime, user);

        List<FileInfo> fileInfos = fileService.findFiles(spec);
        PaginatedResult paginatedResult = fileService.paginateFileInfos(fileInfos, page);

        addAttributesToModel(model, paginatedResult, sortBy, showHidden, authentication);

        return "search";
    }

    public void createDummyFiles(int count) {
        User defaultUser = userService.findByUsername("admin");

        for (int i = 1; i <= count; i++) {
            FileInfo dummyFile = new FileInfo(
                    "ВГМТ." + i + "." + i,
                    "Device " + i,
                    "Type " + i,
                    "Used " + i,
                    "Project " + i,
                    String.valueOf(i),
                    "2024-01-01 00:00:00",
                    "/path/to/location",
                    "2024-01-01 00:00:00",
                    defaultUser
            );
            fileService.saveOrUpdateFile(dummyFile);
        }
    }

    private void addAttributesToModel(Model model, PaginatedResult paginatedResult, String sortBy, Boolean showHidden, Authentication authentication) {
        model.addAttribute("fileInfos", paginatedResult.getPaginatedFileInfos());
        model.addAttribute("hasMoreResults", paginatedResult.isHasMoreResults());
        model.addAttribute("page", paginatedResult.getPage());
        model.addAttribute("pageNumbers", getPageNumbers(paginatedResult.getPage(), paginatedResult.getTotalPages(), 10));
        model.addAttribute("totalPages", paginatedResult.getTotalPages());
        model.addAttribute("nickname", authentication.getName());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("showHidden", showHidden);

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("isDebug", config.getIsDebug());
            model.addAttribute("searchPath", config.getPath());
        }
    }

    public List<Integer> getPageNumbers(int currentPage, int totalPages, int limit) {
        List<Integer> pageNumbers = new ArrayList<>();
        int startPage = Math.max(0, currentPage - limit / 2);
        int endPage = Math.min(startPage + limit, totalPages);

        if (currentPage > 0) {
            pageNumbers.add(-1);
        }

        for (int i = startPage; i < endPage; i++) {
            pageNumbers.add(i);
        }

        if (currentPage < totalPages - 1) {
            pageNumbers.add(totalPages);
        }

        return pageNumbers;
    }
}