package ru.gdlbo.search.searcher.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class LoginController {

    @GetMapping("/login")
    public ModelAndView login(@RequestParam(value = "error", required = false) String error) {

        ModelAndView modelAndView = new ModelAndView("login");
        if (error != null) {
            modelAndView.addObject("error", "Invalid username or password.");
        }

        return modelAndView;
    }

    @GetMapping("/logout")
    public String logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        return "redirect:/auth/login";
    }
}