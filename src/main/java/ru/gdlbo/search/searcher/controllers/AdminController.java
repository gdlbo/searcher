package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gdlbo.search.searcher.config.RestartManager;
import ru.gdlbo.search.searcher.repository.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@RestController
public class AdminController {
    @Autowired
    private RestartManager restartManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/api/grantAdmin")
    public ResponseEntity<String> grantAdminPrivileges(@RequestParam String username, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                            .orElseGet(() -> {
                                Role newRole = new Role("ROLE_ADMIN");
                                roleRepository.save(newRole);
                                return newRole;
                            });

                    if (user.getUserRoles().stream()
                            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_ADMIN"))) {
                        return ResponseEntity.ok("User already an admin");
                    }

                    user.getUserRoles().add(new UserRole(user, adminRole));
                    userRepository.save(user);
                    return ResponseEntity.ok("Admin privileges granted to " + username);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist"));
    }

    @GetMapping("/api/resetDatabase")
    public ResponseEntity<String> resetDatabase(Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin");
        }

        Thread thread = new Thread(() -> {
            userRepository.deleteAll();
            restartManager.restart();
            fileInfoRepository.deleteAll();
        });
        thread.setDaemon(false);
        thread.start();

        return ResponseEntity.ok("Database has been reset");
    }

    @GetMapping("/api/resetFileDatabase")
    public ResponseEntity<String> resetFileDatabase(Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin");
        }

        Thread thread = new Thread(() -> {
            fileInfoRepository.deleteAll();
        });
        thread.setDaemon(false);
        thread.start();

        return ResponseEntity.ok("Database has been reset");
    }

    @GetMapping("/api/restartServer")
    public ResponseEntity<String> restartServer(Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin");
        }

        Thread thread = new Thread(() -> {
            restartManager.restart();
        });
        thread.setDaemon(false);
        thread.start();

        return ResponseEntity.ok("Server is restarting...");
    }

    @GetMapping("/api/changeCredentials")
    public ResponseEntity<String> changeCredentials(@RequestParam(required = false) String username, @RequestParam String oldPassword, @RequestParam(required = false) String newPassword, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Current user not found");
        }

        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }

        if (username != null && !username.isEmpty() && !username.equals(currentUser.getUsername())) {
            boolean exists = userRepository.findByUsername(username).isPresent();
            if (exists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this username already exists");
            }
            currentUser.setUsername(username);
        }

        if (newPassword != null && !newPassword.isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(currentUser);

        return ResponseEntity.ok("Credentials updated successfully");
    }

    @GetMapping("/api/removeFile")
    public String removeFileFromHistory(@RequestParam Long id) {
        System.out.println("Request received to remove file: " + id);

        fileInfoRepository.findById(id).ifPresent(file -> {
            File fileToDelete = new File(file.getLocation());

            new FileHistoryController().deleteFileHistory(fileToDelete.getAbsolutePath());

            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }
        });

        fileInfoRepository.deleteById(id);
        return "redirect:/search";
    }

    @GetMapping("/api/submitCustomPath")
    public ResponseEntity<String> submitCustomPath(@RequestParam String searcherPath) throws IOException {
        Path propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties");

        if (!propertiesPath.toFile().exists()) {
            propertiesPath.toFile().createNewFile();
        }

        Properties properties = new Properties();

        try (FileInputStream inStream = new FileInputStream(propertiesPath.toFile())) {
            properties.load(inStream);
        }

        if (searcherPath == null || searcherPath.isEmpty()) {
            searcherPath = properties.getProperty("searcher.path");
        }

        properties.setProperty("searcher.path", searcherPath);

        try (FileOutputStream outStream = new FileOutputStream(propertiesPath.toFile())) {
            properties.store(outStream, null);
        }

        restartManager.restart();

        return ResponseEntity.ok("Restarting...");
    }

    @GetMapping("/api/dropCustomPath")
    public ResponseEntity<String> dropCustomPath() {
        Path propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties");

        propertiesPath.toFile().delete();

        restartManager.restart();

        return ResponseEntity.ok("Restarting...");
    }
}