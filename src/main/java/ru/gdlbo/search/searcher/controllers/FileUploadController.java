package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.services.FileService;
import ru.gdlbo.search.searcher.services.UserService;

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
                             @RequestParam String lastModified,
                             @RequestParam String creationTime,
                             @RequestParam String inventoryNumber,
                             @RequestParam String location,
                             Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            return "redirect:/error?status=Failed to find user with username " + authentication.getName();
        }

        FileInfo fileInfo = new FileInfo(decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, lastModified, location, creationTime, user);
        fileService.saveOrUpdateFile(fileInfo);
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

            FileInfo fileInfo = new FileInfo(decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, lastModified, location, creationTime, user);

            fileService.saveOrUpdateFile(fileInfo);
            return "redirect:/search";
        } else {
            System.out.println("Failed to find file with id: " + id);
            return "redirect:/error";
        }
    }
}