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
    // Этот метод отвечает за получение истории изменений файла
    @GetMapping("/api/history")
    fun getFileHistory(@RequestParam fileName: String): ResponseEntity<List<FileHistory>> {
        println("Запрос на получение истории файла: $fileName")

        val file = File(fileName) // Создать объект File для исходного файла
        val files = getFiles(fileName) // Получить список файлов-историй

        // Если список файлов пуст, вернуть пустой список
        if (files.isNullOrEmpty()) {
            println("История для файла $fileName не найдена")
            return ResponseEntity.ok(emptyList())
        }

        // Преобразовать список файлов в List и отсортировать по дате изменения
        val fileList: List<File> = files.toList().sortedBy { it.lastModified() }

        val location = file.absolutePath // Получить абсолютный путь исходного файла

        val fileHistory = fileList.map { f -> FileHistory(extractDateFromFileName(f), location) } // Создать объекты FileHistory

        println("Возвращаем историю файла: $fileName")

        return ResponseEntity.ok(fileHistory)
    }

    @GetMapping("/api/removeAllFromHistory")
    fun deleteFileHistory(@RequestParam fileName: String): ResponseEntity<String> {
        println("Запрос на удаление всей истории файла: $fileName")

        val files = getFiles(fileName) // Получить список файлов-историй

        if (files != null) {
            for (historyFile in files) {
                if (historyFile.delete()) {
                    println("Удален файл истории: " + historyFile.name)
                } else {
                    println("Не удалось удалить файл истории: " + historyFile.name)
                    return ResponseEntity("Не удалось удалить файл истории", HttpStatus.BAD_REQUEST)
                }
            }
        } else {
            println("История для файла $fileName не найдена")
            return ResponseEntity("История для файла не найдена", HttpStatus.BAD_REQUEST)
        }
        return ResponseEntity.ok("Файл истории удален")
    }

    // Этот метод отвечает за удаление отдельного файла из истории
    @GetMapping("/api/removeFromHistory")
    fun removeFileFromHistory(@RequestParam filePath: String, authentication: Authentication): ResponseEntity<String> {
        println("Запрос на удаление файла истории: $filePath")

        val fileToRemove = File(filePath) // Создать объект File для файла на удаление

        // Проверка на попытку удаления файлов вне директории истории
        if (!fileToRemove.absolutePath.contains(".history") && !authentication.authorities.contains(
                SimpleGrantedAuthority("ROLE_ADMIN")
            )
        ) {
            println("Попытка удалить файл вне директории истории: $filePath")
            return ResponseEntity.ok("Не удалось удалить файл")
        }

        // Удалить файл из скрытой директории
        if (!fileToRemove.delete()) {
            println("Не удалось удалить файл: $filePath")
            return ResponseEntity.ok("Не удалось удалить файл")
        }

        println("Файл удален: $filePath")

        return ResponseEntity.ok("Файл удален")
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