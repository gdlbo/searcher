package ru.gdlbo.search.searcher.controllers.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import ru.gdlbo.search.searcher.controllers.api.FileReplaceController
import ru.gdlbo.search.searcher.repository.dto.FileInfoDto
import ru.gdlbo.search.searcher.services.FileService
import ru.gdlbo.search.searcher.services.UserService
import java.io.File

@Controller
class ReviewController {
    @Autowired
    private val fileService: FileService? = null // Сервис для работы с файлами

    @Autowired
    private val userService: UserService? = null // Сервис для работы с пользователями

    // Обработка и выдача страницы одобрения файлов (Web)
    @GetMapping("review")
    fun review(model: Model, auth: Authentication): String {
        val fileInfos: List<FileInfoDto?>? // Список информации о файлах

        if (!auth.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            val user = userService!!.findByUsername(auth.name) // Получить пользователя по имени
            fileInfos = fileService!!.findTempByUser(user)?.map { it?.toDTO() } // Найти временные файлы пользователя и преобразовать в DTO
        } else {
            fileInfos = fileService!!.findAllTemp().map { it?.toDTO() } // Найти все временные файлы и преобразовать в DTO
            model.addAttribute("isAdmin", true) // Добавить атрибут в модель, указывающий на роль администратора
        }

        model.addAttribute("fileInfos", fileInfos) // Добавить список файлов в модель
        return "review"
    }

    @PostMapping("/api/web/replaceTempFile")
    @Throws(Exception::class)
    fun replaceTempFile(
        @RequestParam file: MultipartFile,
        @RequestParam fileId: Long,
        request: HttpServletRequest,
        authentication: Authentication
    ): String {
        if (file.isEmpty) {
            return "redirect:/error" // Пустой файл - ошибка
        }

        val fileTempInfo = fileService?.getTempFileById(fileId) // Получить информацию о временном файле по id

        if (fileTempInfo != null && fileTempInfo.isPresent) {
            val fileTempInfo = fileTempInfo.get()
            if (fileTempInfo.user?.username == authentication.name || authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
                // Копирование файла если пользователь администратор или же файл того же пользователя (безопасность)
                FileCopyUtils.copy(file.inputStream.readAllBytes(), File(fileTempInfo.location.toString()))
                return "redirect:/review" // Успешное обновление - переход на страницу отзывов
            } else {
                println("Попытка заменить файл без прав доступа") // Предупреждение о попытке замены без прав
                return "redirect:/error" // Ошибка доступа
            }
        } else {
            println("Файл не существует") // Предупреждение об отсутствии файла
            return "redirect:/error" // Ошибка - файл не найден
        }
    }
}