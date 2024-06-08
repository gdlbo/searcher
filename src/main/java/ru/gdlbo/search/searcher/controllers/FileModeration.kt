package ru.gdlbo.search.searcher.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.repository.dto.FileInfoDto
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
        val fileInfos: List<FileInfoDto?>?

        if (!auth.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            val user = userService!!.findByUsername(auth.name)
            fileInfos = fileService!!.findByUser(user)?.map { it?.toDTO() }
        } else {
            fileInfos = fileService!!.findAllTemp().map { it?.toDTO() }
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

    @GetMapping("/api/getTempList")
    fun tempList(): ResponseEntity<List<FileInfoDto?>> {
        return ResponseEntity.ok().body(fileService!!.findAllTemp().map { it?.toDTO() })
    }
}