package ru.gdlbo.search.searcher.controllers.api

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import ru.gdlbo.search.searcher.services.FileService
import java.io.*

@Controller
class FileReplaceController {
    @Autowired
    private val fileService: FileService? = null

    @PostMapping("/api/replace")
    @Throws(Exception::class)
    fun replaceFile(@RequestParam filePath: String, @RequestParam file: MultipartFile): ResponseEntity<String> {
        println("Request received to replace file: $filePath")

        if (file.isEmpty) {
            return ResponseEntity("File is empty", HttpStatus.BAD_REQUEST)
        }

        val oldFile = File(filePath)

        val hiddenDir = createHiddenDirectory(oldFile)
        checkAndDeleteOldVersions(hiddenDir, oldFile.name)

        replaceOldFileWithNew(file, filePath)

        return ResponseEntity.ok("Successfully replaced file")
    }

    @PostMapping("/api/replaceTempFile")
    @Throws(Exception::class)
    fun replaceTempFile(
        @RequestParam filePath: String,
        @RequestParam file: MultipartFile,
        @RequestParam fileId: Long,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity("File is empty", HttpStatus.BAD_REQUEST)
        }

        val fileTempInfo = fileService?.getTempFileById(fileId)

        if (fileTempInfo != null && fileTempInfo.isPresent) {
            if (fileTempInfo.get().user?.username == authentication.name || authentication.authorities.contains(
                    SimpleGrantedAuthority("ROLE_ADMIN")
                )
            ) {
                FileCopyUtils.copy(file.inputStream.readAllBytes(), File(filePath))
                return ResponseEntity.ok("OK")
            } else {
                println("Trying to replace file without permission")
                return ResponseEntity("Error", HttpStatus.BAD_REQUEST)
            }
        } else {
            println("File isn't exist")
            return ResponseEntity("File isn't exist", HttpStatus.BAD_REQUEST)
        }
    }

    @Throws(Exception::class)
    private fun createHiddenDirectory(oldFile: File): File {
        val hiddenDir = File(oldFile.parentFile, ".history")
        if (!hiddenDir.exists()) {
            if (!hiddenDir.mkdir()) {
                throw Exception("Failed to create directory")
            }
        }
        return hiddenDir
    }

    @Throws(Exception::class)
    private fun checkAndDeleteOldVersions(hiddenDir: File, oldFileName: String) {
        val files = hiddenDir.listFiles { dir: File?, name: String ->
            name.startsWith(
                oldFileName.substring(
                    0,
                    oldFileName.lastIndexOf('.')
                )
            )
        }
        if (files != null && files.size > 10) {
            var oldestFile = files[0]
            for (i in 1 until files.size) {
                if (files[i].lastModified() < oldestFile.lastModified()) {
                    oldestFile = files[i]
                }
            }

            if (!oldestFile.delete()) {
                throw Exception("Failed to delete " + oldestFile.name)
            }

            println("Deleted oldest file: " + oldestFile.name)
        }
    }

    @Throws(IOException::class)
    private fun replaceOldFileWithNew(file: MultipartFile, filePath: String) {
        FileCopyUtils.copy(file.inputStream.readAllBytes(), File(filePath))
        println("Replaced file: $filePath")
    }
}