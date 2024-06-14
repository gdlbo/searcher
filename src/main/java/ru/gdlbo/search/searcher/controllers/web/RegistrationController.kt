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
    private val userRepository: UserRepository? = null // Сервис для работы с пользователями

    @Autowired
    private val roleRepository: RoleRepository? = null // Сервис для работы с ролями пользователей

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null // Сервис для шифрования паролей

    @GetMapping("/auth/register")
    fun showRegistrationForm(): String {
        return "registration"
    }

    @GetMapping("/auth/reg")
    fun registerUser(
        @RequestParam username: String,
        @RequestParam password: String
    ): String {
        if (username.isEmpty() || password.isEmpty()) {
            // Обработать ошибку валидации
            println("Имя пользователя или пароль пусты")
            return "redirect:/auth/register?error" // Перенаправление на форму регистрации с сообщением об ошибке
        }

        // Проверить, существует ли уже пользователь с таким именем
        if (userRepository!!.findByUsername(username).isPresent) {
            // Обработать ошибку занятого имени пользователя
            println("Пользователь уже существует: $username")
            return "redirect:/auth/register?error" // Перенаправление на форму регистрации с сообщением об ошибке
        }

        // Создать нового пользователя
        val user = User()
        user.username = username
        user.password = passwordEncoder!!.encode(password) // Зашифровать пароль

        // Проверить наличие ролей в базе данных
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

        // Назначение стартовой роли
        if (username == "admin") {
            user.userRoles = mutableSetOf(UserRole(user, roleAdmin))
        } else {
            user.userRoles = mutableSetOf(UserRole(user, roleUser))
        }

        // Сохранить пользователя
        userRepository.save(user)

        if (userRepository.findByUsername(username).isPresent) {
            println("Пользователь успешно создан: $username")
            return "redirect:/auth/login" // Перенаправление на страницу входа после успешной регистрации
        } else {
            println("Не удалось создать пользователя: $username")
            return "redirect:/error" // Перенаправление на страницу ошибки в случае неудачной регистрации
        }
    }
}