package ru.gdlbo.search.searcher.controllers.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.repository.dto.FileInfoDto
import ru.gdlbo.search.searcher.services.FileService

@Controller
class FileModeration {
    @Autowired
    private val fileService: FileService? = null


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