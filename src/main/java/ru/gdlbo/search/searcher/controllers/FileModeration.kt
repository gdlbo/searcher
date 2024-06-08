package ru.gdlbo.search.searcher.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.repository.FileTempInfo
import ru.gdlbo.search.searcher.services.FileService
import ru.gdlbo.search.searcher.services.UserService

@Controller
class FileModeration {
    @Autowired
    private val fileService: FileService? = null

    @Autowired
    private val userService: UserService? = null

    @GetMapping("review")
    fun review(model: Model, auth: Authentication): String {
        val fileInfos: List<FileTempInfo?>?

        if (!auth.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            val user = userService!!.findByUsername(auth.name)
            fileInfos = fileService!!.findByUser(user)
        } else {
            fileInfos = fileService!!.findAllTemp()
            model.addAttribute("isAdmin", true)
        }

        model.addAttribute("fileInfos", fileInfos)
        return "review"
    }

    @GetMapping("/api/approve")
    fun approve(@RequestParam id: Long): ResponseEntity<Void> {
        fileService!!.approveTempFile(id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/refuse")
    fun decline(@RequestParam id: Long): ResponseEntity<Void> {
        fileService!!.removeTempFile(id)
        return ResponseEntity.ok().build()
    }

    @get:GetMapping("/api/getTempList")
    val tempList: List<FileTempInfo?>
        get() = fileService!!.findAllTemp()
}