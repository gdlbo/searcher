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
    private val fileService: FileService? = null // Сервис для работы с файлами

    // Замена файла и сохранение в историю (API)
    @PostMapping("/api/replace")
    @Throws(Exception::class)
    fun replaceFile(@RequestParam filePath: String, @RequestParam file: MultipartFile): ResponseEntity<String> {
        println("Запрос на замену файла: $filePath")

        // Проверка, что загруженный файл не пустой
        if (file.isEmpty) {
            return ResponseEntity("Файл пустой", HttpStatus.BAD_REQUEST)
        }

        val oldFile = File(filePath) // Получение объекта File для исходного файла

        // Создание скрытой директории для хранения истории версий файла (если она не существует)
        val hiddenDir = createHiddenDirectory(oldFile)

        // Проверка и удаление старых версий файла (если их количество превышает 10)
        checkAndDeleteOldVersions(hiddenDir, oldFile.name)

        // Замена старого файла новым
        replaceOldFileWithNew(file, filePath)

        return ResponseEntity.ok("Файл успешно заменен")
    }

    // Замена не одобренного файла (API)
    @PostMapping("/api/replaceTempFile")
    @Throws(Exception::class)
    fun replaceTempFile(
        @RequestParam filePath: String,
        @RequestParam file: MultipartFile,
        @RequestParam fileId: Long,
        request: HttpServletRequest,
        authentication: Authentication
    ): ResponseEntity<String> {
        // Проверка, что загруженный файл не пустой
        if (file.isEmpty) {
            return ResponseEntity("Файл пустой", HttpStatus.BAD_REQUEST)
        }

        // Поиск временного файла по id
        val fileTempInfo = fileService?.getTempFileById(fileId)

        if (fileTempInfo != null && fileTempInfo.isPresent) {
            // Проверка прав на замену временного файла
            if (fileTempInfo.get().user?.username == authentication.name || authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
                // Копирование содержимого нового файла во временный файл
                FileCopyUtils.copy(file.inputStream.readAllBytes(), File(filePath))
                return ResponseEntity.ok("OK")
            } else {
                println("Попытка заменить файл без прав")
                return ResponseEntity("Ошибка", HttpStatus.BAD_REQUEST)
            }
        } else {
            println("Файл не найден")
            return ResponseEntity("Файл не найден", HttpStatus.BAD_REQUEST)
        }
    }

    @Throws(Exception::class)
    private fun createHiddenDirectory(oldFile: File): File {
        // Создание скрытой директории для хранения истории версий файла
        val hiddenDir = File(oldFile.parentFile, ".history")
        if (!hiddenDir.exists()) { // Проверка существования директории
            if (!hiddenDir.mkdir()) {
                throw Exception("Не удалось создать директорию")
            }
        }
        return hiddenDir
    }

    @Throws(Exception::class)
    private fun checkAndDeleteOldVersions(hiddenDir: File, oldFileName: String) {
        // Поиск старых версий файла в скрытой директории
        val files = hiddenDir.listFiles { dir: File?, name: String ->
            name.startsWith(
                oldFileName.substring(
                    0,
                    oldFileName.lastIndexOf('.')
                )
            )
        }

        // Удаление самой старой версии файла, если количество версий превышает 10
        if (files != null && files.size > 10) {
            var oldestFile = files[0]
            for (i in 1 until files.size) {
                if (files[i].lastModified() < oldestFile.lastModified()) {
                    oldestFile = files[i]
                }
            }

            if (!oldestFile.delete()) {
                throw Exception("Не удалось удалить " + oldestFile.name)
            }

            println("Удален самый старый файл: " + oldestFile.name)
        }
    }

    @Throws(IOException::class)
    private fun replaceOldFileWithNew(file: MultipartFile, filePath: String) {
        // Замена старого файла новым
        FileCopyUtils.copy(file.inputStream.readAllBytes(), File(filePath))
        println("Файл $filePath заменен")
    }
}
