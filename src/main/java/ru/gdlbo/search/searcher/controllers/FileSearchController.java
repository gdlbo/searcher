package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.FileInfoSpecification;
import ru.gdlbo.search.searcher.repository.PaginatedResult;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.services.FileService;
import ru.gdlbo.search.searcher.services.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class FileSearchController {
    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public String searchFiles(@RequestParam(defaultValue = "0") int page,
                              @RequestParam Map<String, String> formData,
                              Authentication authentication,
                              Model model) {

        String decNumber = formData.get("decNumber");
        String deviceName = formData.get("deviceName");
        String documentType = formData.get("documentType");
        String usedDevices = formData.get("usedDevices");
        String project = formData.get("project");
        String inventoryNumber = formData.get("inventoryNumber");
        String lastModified = formData.get("lastModified");
        String location = formData.get("location");
        String creationTime = formData.get("creationTime");

        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/error";
        }

        if (!fileService.isAnyFilePresent()) {
            createDummyFiles(500);
        }

        if (decNumber != null && !decNumber.isEmpty() && !decNumber.startsWith("ВГМТ.")) {
            decNumber = "ВГМТ." + decNumber;
        }

        Specification<FileInfo> spec = FileInfoSpecification.createSpecification(
                decNumber, deviceName, documentType, usedDevices, project,
                inventoryNumber, lastModified, location, creationTime);

        List<FileInfo> fileInfos = fileService.findFiles(spec);
        PaginatedResult paginatedResult = fileService.paginateFileInfos(fileInfos, page);

        addAttributesToModel(model, paginatedResult, authentication);

        setAttr(model, "decNumber", decNumber);
        setAttr(model, "deviceName", deviceName);
        setAttr(model, "documentType", documentType);
        setAttr(model, "usedDevices", usedDevices);
        setAttr(model, "project", project);
        setAttr(model, "inventoryNumber", inventoryNumber);
        setAttr(model, "creationTime", creationTime);
        setAttr(model, "lastModified", lastModified);
        setAttr(model, "location", location);

        return "search";
    }

    @GetMapping("/api/searchFile")
    public ResponseEntity<FileInfo> searchFiles(@RequestParam Long id) {
        return fileService.getFileById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public void createDummyFiles(int count) {
        User defaultUser = userService.findByUsername("admin");
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 1; i <= count; i++) {
            int randomNumber = random.nextInt(1000);
            String fileName = String.format("ВГМТ.%06d.%03d МАРШРУТИЗАТОР INCARNET %d.pdf", 465245 + i, 7, randomNumber);

            LocalDateTime randomDateTime = LocalDateTime.now().plusDays(random.nextInt(365));
            String formattedDateTime = randomDateTime.format(formatter);

            FileInfo dummyFile = new FileInfo(
                    fileName,
                    "Device " + i,
                    "Type " + i,
                    "Used " + i,
                    "Project " + i,
                    String.valueOf(i),
                    formattedDateTime,
                    "/path/to/location",
                    formattedDateTime,
                    defaultUser
            );
            fileService.saveOrUpdateFile(dummyFile);
        }
    }

    private void addAttributesToModel(Model model, PaginatedResult paginatedResult, Authentication authentication) {
        model.addAttribute("fileInfos", paginatedResult.getPaginatedFileInfos());
        model.addAttribute("hasMoreResults", paginatedResult.isHasMoreResults());
        model.addAttribute("page", paginatedResult.getPage());
        model.addAttribute("pageNumbers", getPageNumbers(paginatedResult.getPage(), paginatedResult.getTotalPages(), 10));
        model.addAttribute("totalPages", paginatedResult.getTotalPages());
        model.addAttribute("nickname", authentication.getName());

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            model.addAttribute("isAdmin", true);
        }
    }

    private void setAttr(Model model, String attr, String value) {
        if (value != null && !value.isEmpty()) {
            model.addAttribute(attr, value);
        }
    }

    public List<Integer> getPageNumbers(int currentPage, int totalPages, int limit) {
        List<Integer> pageNumbers = new ArrayList<>();
        int startPage = Math.max(0, currentPage - limit / 2);
        int endPage = Math.min(startPage + limit, totalPages);

        if (startPage > 0) {
            pageNumbers.add(-1);
        }

        for (int i = startPage; i < endPage; i++) {
            pageNumbers.add(i);
        }

        if (endPage < totalPages) {
            pageNumbers.add(totalPages);
        }

        return pageNumbers;
    }
}