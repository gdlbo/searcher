package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import ru.gdlbo.search.searcher.config.RestartManager;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.repository.UserRepository;
import ru.gdlbo.search.searcher.repository.UserRole;

import java.util.Optional;

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

    @GetMapping("/api/changeCredentials")
    public ResponseEntity<String> changeCredentials(@RequestParam(required = false) String username, @RequestParam String oldPassword, @RequestParam String newPassword, Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Current user not found");
        }

        if (!currentUser.getPassword().equals(oldPassword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }

        if (username != null && !username.isEmpty() && !username.equals(currentUser.getUsername())) {
            boolean exists = userRepository.findByUsername(username).isPresent();
            if (exists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this username already exists");
            }
            currentUser.setUsername(username);
        }

        currentUser.setPassword(newPassword);
        userRepository.save(currentUser);

        return ResponseEntity.ok("Credentials updated successfully");
    }
}