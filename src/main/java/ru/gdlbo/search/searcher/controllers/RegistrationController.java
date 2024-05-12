package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.config.WebSecurityConfig;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.repository.UserRepository;
import ru.gdlbo.search.searcher.repository.UserRole;

import java.util.Collections;

@Controller
public class RegistrationController {
    @Autowired
    private UserRepository userRepository;

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
        user.setPassword(new WebSecurityConfig().passwordEncoder().encode(password));

        if (username.equals("admin")) {
            user.setUserRoles(Collections.singleton(new UserRole("ROLE_ADMIN", user)));
        } else {
            user.setUserRoles(Collections.singleton(new UserRole("ROLE_USER", user)));
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