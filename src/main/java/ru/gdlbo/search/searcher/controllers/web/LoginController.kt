package ru.gdlbo.search.searcher.controllers.web

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/auth")
class LoginController {
    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping("/logout")
    fun logout(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null) {
            SecurityContextHolder.getContext().authentication = null
        }
        return "redirect:/auth/login"
    }
}