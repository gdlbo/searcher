package ru.gdlbo.search.searcher.controllers.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.repository.files.FileHistory
import java.io.File
import java.util.regex.Pattern

@Controller
class FileHistoryController {
    // This method is responsible for retrieving the history of a file
    @GetMapping("/api/history")
    fun getFileHistory(@RequestParam fileName: String): ResponseEntity<List<FileHistory>> {
        println("Request received to get history for file: $fileName")

        val file = File(fileName)
        val files = getFiles(fileName)

        // Если нет файлов, возвращаем пустой список
        if (files.isNullOrEmpty()) {
            println("No history found for file: $fileName")
            return ResponseEntity.ok(emptyList())
        }

        // Сортируем файлы по времени последнего изменения
        val fileList: List<File> = files.toList().sortedBy { it.lastModified() }
        val location = file.absolutePath

        val fileHistory = fileList.map { f -> FileHistory(extractDateFromFileName(f), location) }

        println("Returning history for file: $fileName")

        return ResponseEntity.ok(fileHistory)
    }

    @GetMapping("/api/removeAllFromHistory")
    fun deleteFileHistory(@RequestParam fileName: String): ResponseEntity<String> {
        println("Request received to delete all history for file: $fileName")

        val files = getFiles(fileName)

        if (files != null) {
            for (historyFile in files) {
                if (historyFile.delete()) {
                    println("Deleted history file: " + historyFile.name)
                } else {
                    println("Failed to delete history file: " + historyFile.name)
                    return ResponseEntity("Failed to delete history file", HttpStatus.BAD_REQUEST)
                }
            }
        } else {
            println("No history found for file: $fileName")
            return ResponseEntity("No history found for file", HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity.ok("Deleted file")
    }

    // This method is responsible for removing a file from the history
    @GetMapping("/api/removeFromHistory")
    fun removeFileFromHistory(@RequestParam filePath: String, authentication: Authentication): ResponseEntity<String> {
        println("Request received to remove file history: $filePath")

        val fileToRemove = File(filePath)

        // Check to not abuse deletion of files
        if (!fileToRemove.absolutePath.contains(".history") && !authentication.authorities.contains(
                SimpleGrantedAuthority("ROLE_ADMIN")
            )
        ) {
            println("Attempt to delete file outside of history directory: $filePath")
            return ResponseEntity.ok("Failed to delete file")
        }

        // Remove the file from the hidden directory
        if (!fileToRemove.delete()) {
            println("Failed to delete file: $filePath")
            return ResponseEntity.ok("Failed to delete file")
        }

        println("Deleted file: $filePath")

        return ResponseEntity.ok("Deleted file")
    }

    companion object {
        private fun getFiles(path: String): Array<File>? {
            val file = File(path)
            val parentDir = file.parentFile
            val hiddenDir = File(parentDir, ".history")

            return hiddenDir.listFiles { _: File?, name: String ->
                var baseName = file.name
                val lastDotIndex = baseName.lastIndexOf('.')
                if (lastDotIndex != -1) {
                    baseName = baseName.substring(0, lastDotIndex)
                }

                var currentFileNameWithoutExtension = name
                val currentFileLastDotIndex = currentFileNameWithoutExtension.lastIndexOf('.')
                if (currentFileLastDotIndex != -1) {
                    currentFileNameWithoutExtension =
                        currentFileNameWithoutExtension.substring(0, currentFileLastDotIndex)
                }
                currentFileNameWithoutExtension.startsWith(baseName)
            }
        }

        private fun extractDateFromFileName(file: File): String {
            val fileName = file.name
            val datePattern = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}"

            val pattern = Pattern.compile(datePattern)
            val matcher = pattern.matcher(fileName)

            return if (matcher.find()) {
                matcher.group(0).replace("_", " ")
            } else {
                "N/A"
            }
        }
    }
}