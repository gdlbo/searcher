package ru.gdlbo.search.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.gdlbo.search.searcher.repository.FileTempInfo;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.services.FileService;
import ru.gdlbo.search.searcher.services.UserService;

import java.util.List;

@Controller
public class FileModeration {
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;

    @GetMapping("review")
    public String review(Model model, Authentication auth) {
        List<FileTempInfo> fileInfos;

        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            User user = userService.findByUsername(auth.getName());
            fileInfos = fileService.findByUser(user);
        } else {
            fileInfos = fileService.findAllTemp();
            model.addAttribute("isAdmin", true);
        }

        model.addAttribute("fileInfos", fileInfos);
        return "review";
    }

    @GetMapping("/api/approve")
    public void approve(@RequestParam Long id) {
        fileService.approveTempFile(id);
    }

    @GetMapping("/api/refuse")
    public void decline(@RequestParam Long id) {
        fileService.removeTempFile(id);
    }

    @GetMapping("/api/getTempList")
    public List<FileTempInfo> getTempList() {
        return fileService.findAllTemp();
    }
}