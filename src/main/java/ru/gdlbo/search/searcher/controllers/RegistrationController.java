package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.*;

import java.util.Collections;

@Controller
public class RegistrationController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/auth/register")
    public String showRegistrationForm() {
        return "registration";
    }

    @GetMapping("/auth/reg")
    public String registerUser(@RequestParam String username, @RequestParam String password) {
        if (username.isEmpty() || password.isEmpty()) {
            // Handle validation error
            System.out.println("Username or password is empty");
            return "redirect:/auth/register?error";
        }

        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            // Handle duplicate username error
            System.out.println("User already exists: " + username);
            return "redirect:/auth/register?error";
        }

        // Create a new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        // Ensure roles exist in the database
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role newRole = new Role("ROLE_ADMIN");
            roleRepository.save(newRole);
            return newRole;
        });

        Role roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role newRole = new Role("ROLE_USER");
            roleRepository.save(newRole);
            return newRole;
        });

        if (username.equals("admin")) {
            user.setUserRoles(Collections.singleton(new UserRole(user, roleAdmin)));
        } else {
            user.setUserRoles(Collections.singleton(new UserRole(user, roleUser)));
        }

        // Save the user
        userRepository.save(user);

        if (userRepository.findByUsername(username).isPresent()) {
            System.out.println("User saved successfully: " + username);
            return "redirect:/auth/login";
        } else {
            System.out.println("Failed to save user: " + username);
            return "redirect:/error";
        }
    }
}