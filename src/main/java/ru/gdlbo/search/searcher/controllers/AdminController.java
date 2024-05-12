package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import ru.gdlbo.search.searcher.SearcherApplication;
import ru.gdlbo.search.searcher.config.RestartManager;
import ru.gdlbo.search.searcher.repository.UserRepository;
import ru.gdlbo.search.searcher.repository.UserRole;

@RestController
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestartManager restartManager;

    @GetMapping("/api/grantAdmin")
    public ResponseEntity<String> grantAdminPrivileges(@RequestParam String username, Authentication authentication) {
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (user.getUserRoles().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))) {
                        return ResponseEntity.ok("User already an admin");
                    }
                    user.getUserRoles().add(new UserRole("ROLE_ADMIN", user));
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

    @GetMapping("/api/changePassword")
    public ResponseEntity<String> changePassword(@RequestParam(required = false) String username , @RequestParam String newPassword, Authentication authentication) {
        if (username == null || username.isEmpty()) {
            username = authentication.getName();
        }

        if (!authentication.getName().equals(username) || !authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not same as username");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setPassword(newPassword);
                    userRepository.save(user);
                    return ResponseEntity.ok("Password changed");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist"));
    }
}