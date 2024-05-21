package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.services.FileService;
import ru.gdlbo.search.searcher.services.UserService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
public class FileUploadController {
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;

    @PostMapping("/api/upload")
    public String uploadFile(@RequestParam String decNumber,
                             @RequestParam String deviceName,
                             @RequestParam String documentType,
                             @RequestParam String usedDevices,
                             @RequestParam String project,
                             @RequestParam String inventoryNumber,
                             @RequestParam String location,
                             @RequestParam MultipartFile file,
                             Authentication authentication) throws Exception {

        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/error?status=Failed to find user with username " + authentication.getName();
        }

        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }

        String creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String lastModified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String locationWithFileName = location + "/" + file.getOriginalFilename();

        FileInfo fileInfo = new FileInfo(decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, lastModified, locationWithFileName, creationTime, user);
        fileService.saveOrUpdateFile(fileInfo);

        FileCopyUtils.copy(file.getInputStream().readAllBytes(), new File(locationWithFileName));
        System.out.println("Uploaded file: " + locationWithFileName);

        return "redirect:/search";
    }

    @PostMapping("/api/update")
    public String updateFile(@RequestParam Long id,
                             @RequestParam String decNumber,
                             @RequestParam String deviceName,
                             @RequestParam String documentType,
                             @RequestParam String usedDevices,
                             @RequestParam String project,
                             @RequestParam String lastModified,
                             @RequestParam String creationTime,
                             @RequestParam String inventoryNumber,
                             @RequestParam String location,
                             @RequestParam String userName,
                             Authentication authentication) {

        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/error";
        }

        Optional<FileInfo> optionalFileInfo = fileService.getFileById(id);
        if (optionalFileInfo.isPresent()) {
            User user = userService.findByUsername(userName);

            if (user == null) {
                System.out.println("Failed to find user with username: " + userName);
                return "redirect:/error";
            }

            String formattedLastModified = formatDateTime(lastModified);
            String formattedCreationTime = formatDateTime(creationTime);

            FileInfo fileInfo = new FileInfo(decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, formattedLastModified, location, formattedCreationTime, user);

            fileService.saveOrUpdateFile(fileInfo);
            return "redirect:/search";
        } else {
            System.out.println("Failed to find file with id: " + id);
            return "redirect:/error";
        }
    }

    private String formatDateTime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}