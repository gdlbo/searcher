package ru.gdlbo.search.searcher.controllers.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.repository.role.Role
import ru.gdlbo.search.searcher.repository.role.RoleRepository
import ru.gdlbo.search.searcher.repository.user.User
import ru.gdlbo.search.searcher.repository.user.UserRepository
import ru.gdlbo.search.searcher.repository.user.UserRole

@Controller
class RegistrationController {
    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val roleRepository: RoleRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @GetMapping("/auth/register")
    fun showRegistrationForm(): String {
        return "registration"
    }

    @GetMapping("/auth/reg")
    fun registerUser(@RequestParam username: String, @RequestParam password: String): String {
        if (username.isEmpty() || password.isEmpty()) {
            // Handle validation error
            println("Username or password is empty")
            return "redirect:/auth/register?error"
        }

        // Check if username already exists
        if (userRepository!!.findByUsername(username).isPresent) {
            // Handle duplicate username error
            println("User already exists: $username")
            return "redirect:/auth/register?error"
        }

        // Create a new user
        val user = User()
        user.username = username
        user.password = passwordEncoder!!.encode(password)

        // Ensure roles exist in the database
        val roleAdmin = roleRepository!!.findByName("ROLE_ADMIN").orElseGet {
            val newRole = Role("ROLE_ADMIN")
            roleRepository.save(newRole)
            newRole
        }

        val roleUser = roleRepository.findByName("ROLE_USER").orElseGet {
            val newRole = Role("ROLE_USER")
            roleRepository.save(newRole)
            newRole
        }

        if (username == "admin") {
            user.userRoles = mutableSetOf(UserRole(user, roleAdmin))
        } else {
            user.userRoles = mutableSetOf(UserRole(user, roleUser))
        }

        // Save the user
        userRepository.save(user)

        if (userRepository.findByUsername(username).isPresent) {
            println("User saved successfully: $username")
            return "redirect:/auth/login"
        } else {
            println("Failed to save user: $username")
            return "redirect:/error"
        }
    }
}