package ru.gdlbo.search.searcher.controllers.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.repository.dto.FileInfoDto
import ru.gdlbo.search.searcher.repository.files.FileInfo
import ru.gdlbo.search.searcher.repository.files.FileTempInfo
import ru.gdlbo.search.searcher.services.FileService
import ru.gdlbo.search.searcher.services.UserService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Controller
class FileSearchController {
    @Autowired
    private val fileService: FileService? = null

    @Autowired
    private val userService: UserService? = null

    @GetMapping("/api/setDummyFiles")
    fun setDummyFiles(): ResponseEntity<String> {
        createDummyFiles(500)
        return ResponseEntity.ok("Dummy files created successfully")
    }

    @GetMapping("/api/searchFile")
    fun searchFiles(@RequestParam id: Long): ResponseEntity<FileInfoDto>? {
        return fileService!!.getFileById(id)
            .map<ResponseEntity<FileInfoDto>> { body: FileInfo? -> ResponseEntity.ok(body?.toDTO()) }
            .orElseGet { ResponseEntity.status(HttpStatus.NOT_FOUND).build() }
    }

    @GetMapping("/api/searchTempFile")
    fun searchTempFiles(@RequestParam id: Long): ResponseEntity<FileInfoDto> {
        return fileService!!.getTempFileById(id)
            .map<ResponseEntity<FileInfoDto>> { body: FileTempInfo? -> ResponseEntity.ok(body?.toDTO()) }
            .orElseGet { ResponseEntity.status(HttpStatus.NOT_FOUND).build() }
    }

    private fun createDummyFiles(count: Int) {
        val defaultUser = userService!!.findByUsername("admin")
        val random = Random()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        for (i in 1..count) {
            val randomNumber = random.nextInt(1000)
            val fileName = String.format("ВГМТ.%06d.%03d МАРШРУТИЗАТОР INCARNET %d.pdf", 465245 + i, 7, randomNumber)

            val randomDateTime = LocalDateTime.now().plusDays(random.nextInt(365).toLong())
            val formattedDateTime = randomDateTime.format(formatter)

            val dummyFile = FileInfo(
                fileName,
                "Device $i",
                "Type $i",
                "Used $i",
                "Project $i",
                i.toString(),
                formattedDateTime,
                "/path/to/location",
                formattedDateTime,
                defaultUser!!
            )
            fileService!!.saveOrUpdateFile(dummyFile)
        }
    }
}