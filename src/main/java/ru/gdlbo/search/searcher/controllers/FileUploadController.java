package ru.gdlbo.search.searcher.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.gdlbo.search.searcher.config.Config;
import ru.gdlbo.search.searcher.repository.FileInfo;
import ru.gdlbo.search.searcher.repository.FileTempInfo;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.services.FileService;
import ru.gdlbo.search.searcher.services.UserService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class FileUploadController {
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private Config config;

    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam String decNumber,
            @RequestParam String deviceName,
            @RequestParam String documentType,
            @RequestParam(required = false) String usedDevices,
            @RequestParam String project,
            @RequestParam String inventoryNumber,
            @RequestParam(required = false) String location,
            @RequestParam MultipartFile file,
            Authentication authentication) throws Exception {

        Map<String, String> response = new HashMap<>();
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            response.put("error", "Ошибка: Не удалось найти пользователя " + authentication.getName());
            return ResponseEntity.badRequest().body(response);
        }

        if (file.isEmpty()) {
            response.put("error", "Файл не может быть пустым");
            return ResponseEntity.badRequest().body(response);
        }

        if (fileService.existsByDecNumber(decNumber)) {
            response.put("error", "Такой децимальный номер уже существует");
            return ResponseEntity.badRequest().body(response);
        }

        if (config.getPath() != null) {
            location = config.getPath();
        }

        if (usedDevices == null || usedDevices.isEmpty()) {
            usedDevices = "N/A";
        }

        String creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String lastModified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String locationWithFileName;

        if (isAdmin(authentication)) {
            locationWithFileName = location + "/" + file.getOriginalFilename();
        } else {
            File targetDirectory = createHiddenDirectory(new File(location));
            locationWithFileName = targetDirectory.getPath() + "/" + file.getOriginalFilename();
        }

        FileInfo fileInfo = new FileInfo(decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, lastModified, locationWithFileName, creationTime, user);

        if (isAdmin(authentication)) {
            fileService.saveOrUpdateFile(fileInfo);
        } else {
            fileService.saveTempFile(new FileTempInfo(fileInfo));
        }

        File newFile = new File(locationWithFileName);
        FileCopyUtils.copy(file.getInputStream().readAllBytes(), newFile);

        System.out.println("Uploaded file: " + locationWithFileName);

        response.put("success", "Файл успешно загружен");
        return ResponseEntity.ok(response);
    }

    private File createHiddenDirectory(File oldFile) throws Exception {
        File hiddenDir = new File(oldFile, ".review");
        if (!hiddenDir.exists()) {
            if (!hiddenDir.mkdir()) {
                throw new Exception("Failed to create directory");
            }
        }
        return hiddenDir;
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
                             @RequestParam(required = false) String location,
                             @RequestParam(required = false) String userName,
                             @RequestParam(defaultValue = "false", required = false) Boolean isReview,
                             HttpServletRequest request,
                             Authentication authentication) {

        if (!isReview && !isAdmin(authentication)) {
            return "redirect:/error";
        }

        Optional<FileInfo> optionalFileInfo = fileService.getFileById(id);
        Optional<FileTempInfo> optionalTempFileInfo = fileService.getTempFileById(id);
        String referer = request.getHeader("Referer");

        if (optionalTempFileInfo.isPresent() || optionalFileInfo.isPresent()) {
            if (isReview) {
                location = optionalTempFileInfo.get().getLocation();
                userName = authentication.getName();
            }

            User user = userService.findByUsername(userName);

            if (user == null || (!user.getUsername().equals(userName) && isReview && !isAdmin(authentication))) {
                System.out.println("Failed to find user with username: " + userName);
                return "redirect:/error";
            }

            String formattedLastModified = formatDateTime(lastModified);
            String formattedCreationTime = formatDateTime(creationTime);

            if (isReview) {
                saveTempFile(id, decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, formattedLastModified, location, formattedCreationTime, user);
            } else {
                saveFile(id, decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, formattedLastModified, location, formattedCreationTime, user);
            }
            return "redirect:" + referer;
        } else {
            System.out.println("Failed to find file with id: " + id);
            return "redirect:/error";
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private void saveTempFile(Long id, String decNumber, String deviceName, String documentType, String usedDevices, String project, String inventoryNumber, String formattedLastModified, String location, String formattedCreationTime, User user) {
        FileTempInfo fileInfo = new FileTempInfo(id, decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, formattedLastModified, location, formattedCreationTime, user);
        fileService.saveTempFile(fileInfo);
    }

    private void saveFile(Long id, String decNumber, String deviceName, String documentType, String usedDevices, String project, String inventoryNumber, String formattedLastModified, String location, String formattedCreationTime, User user) {
        FileInfo fileInfo = new FileInfo(id, decNumber, deviceName, documentType, usedDevices, project, inventoryNumber, formattedLastModified, location, formattedCreationTime, user);
        fileService.saveOrUpdateFile(fileInfo);
    }

    private String formatDateTime(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime);
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}