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
import ru.gdlbo.search.searcher.config.Config
import ru.gdlbo.search.searcher.controllers.api.FileReplaceController
import ru.gdlbo.search.searcher.repository.PaginatedResult
import ru.gdlbo.search.searcher.repository.files.FileInfoSpecification
import ru.gdlbo.search.searcher.services.FileService
import ru.gdlbo.search.searcher.services.UserService
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

@Controller
class SearchController {
    @Autowired
    private val fileService: FileService? = null  // Сервис для работы с файлами

    @Autowired
    private val userService: UserService? = null  // Сервис для работы с пользователями

    @Autowired
    private val config: Config? = null

    @PostMapping("/api/web/replaceFile")
    @Throws(Exception::class)
    fun replaceTempFile(
        @RequestParam filePath: String,
        @RequestParam file: MultipartFile,
        authentication: Authentication
    ): String {
        println("Получен запрос на замену файла: $filePath")  // Печатает в консоль полученный путь к файлу

        if (file.isEmpty) {
            return "redirect:/error"
        }

        val oldFile = File(filePath)

        val hiddenDir = createHiddenDirectory(oldFile)  // Создает скрытую директорию для истории файлов
        checkAndDeleteOldVersions(hiddenDir, oldFile.name)  // Проверяет и удаляет старые версии файла

        replaceOldFileWithNew(file, filePath)  // Заменяет старый файл новым

        return "redirect:/search"
    }

    @Throws(Exception::class)
    private fun createHiddenDirectory(oldFile: File): File {
        // Создаем объект директории с именем ".history" в родительской директории переданного файла
        val hiddenDir = File(oldFile.parentFile, ".history")

        // Проверяем, существует ли уже эта директория
        if (!hiddenDir.exists()) {
            // Если директории не существует, пытаемся ее создать
            if (!hiddenDir.mkdir()) {
                // Если не удалось создать директорию, выбрасываем исключение с сообщением об ошибке
                throw Exception("Не удалось создать директорию")
            }
        }

        // Возвращаем объект созданной или уже существующей директории
        return hiddenDir
    }

    @Throws(Exception::class)
    private fun checkAndDeleteOldVersions(hiddenDir: File, oldFileName: String) {
        // Получаем список файлов в директории, которые начинаются с имени переданного файла (без расширения)
        val files = hiddenDir.listFiles { dir: File?, name: String ->
            name.startsWith(
                oldFileName.substring(
                    0,
                    oldFileName.lastIndexOf('.')
                )
            )
        }

        // Проверяем, если файлов больше 10 (лимит по рекомендации компании)
        if (files != null && files.size > 10) {
            // Инициализируем переменную для самого старого файла, начально задав ей первый файл в списке
            var oldestFile = files[0]

            // Проходим по остальным файлам и находим самый старый файл
            for (i in 1 until files.size) {
                if (files[i].lastModified() < oldestFile.lastModified()) {
                    oldestFile = files[i]
                }
            }

            // Пытаемся удалить самый старый файл
            if (!oldestFile.delete()) {
                // Если не удалось удалить файл, выбрасываем исключение с сообщением об ошибке
                throw Exception("Не удалось удалить " + oldestFile.name)
            }

            // Печатаем сообщение об успешном удалении самого старого файла
            println("Удален самый старый файл: " + oldestFile.name)
        }
    }

    @Throws(IOException::class)
    private fun replaceOldFileWithNew(file: MultipartFile, filePath: String) {
        FileCopyUtils.copy(file.inputStream.readAllBytes(), File(filePath))
        println("Заменен файл: $filePath")  // Печатает в консоль путь к замененному файлу
    }

    // Обработка поисковых запросов и выдача как страницы (Web)
    @GetMapping("/search")
    fun searchFiles(
        // Номер страницы (по умолчанию 0)
        @RequestParam(defaultValue = "0") page: Int,
        // Данные формы (ключ-значение)
        @RequestParam formData: Map<String?, String?>,
        // Данные авторизации
        authentication: Authentication,
        // Модель для представления данных
        model: Model
    ): String {
        // Проверка роли администратора
        val isAdmin = authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))

        // Проверка наличия кастомного пути
        val hasCustomPath = config?.path != null

        // Получение данных из формы
        val decNumber = formData["decNumber"]
        val deviceName = formData["deviceName"]
        val documentType = formData["documentType"]
        val usedDevices = formData["usedDevices"]
        val project = formData["project"]
        val inventoryNumber = formData["inventoryNumber"]
        val lastModified = formData["lastModified"]
        val location = formData["location"]
        val creationTime = formData["creationTime"]

        // Получение имени пользователя (своего, если админ, иначе из авторизации)
        val userName = if (isAdmin) formData["user"] else authentication.name

        // Поиск пользователя по имени (если не найден - редирект на ошибку)
        userService!!.findByUsername(authentication.name) ?: return "redirect:/error"

        // Формирование спецификации поиска файлов
        val spec = FileInfoSpecification.createSpecification(
            decNumber, deviceName, documentType, usedDevices, project,
            inventoryNumber, lastModified, location, creationTime, userName
        )

        // Поиск файлов по спецификации
        val files = fileService!!.findFiles(spec)

        // Получение постраничных результатов поиска
        val paginatedResult = fileService.paginateFileInfos(files, page)

        // Добавление атрибутов в модель (результаты поиска, пагинация и т.д.)
        addAttributesToModel(model, paginatedResult, authentication)

        // Добавление отдельных атрибутов в модель (данные из формы)
        setAttr(model, "decNumber", decNumber)
        setAttr(model, "deviceName", deviceName)
        setAttr(model, "documentType", documentType)
        setAttr(model, "usedDevices", usedDevices)
        setAttr(model, "project", project)
        setAttr(model, "inventoryNumber", inventoryNumber)
        setAttr(model, "creationTime", creationTime)
        setAttr(model, "lastModified", lastModified)
        setAttr(model, "location", location)

        // Атрибут для отключения кастомного пути (если он не задан)
        model.addAttribute("isCustomPathDisabled", !hasCustomPath)

        // Добавление кастомного пути в модель для администраторов
        if (hasCustomPath && isAdmin) {
            model.addAttribute("path", config?.path)
        }

        return "search" // название шаблона для отображения результатов
    }

    private fun addAttributesToModel(model: Model, paginatedResult: PaginatedResult, authentication: Authentication) {
        // Результаты поиска
        model.addAttribute("fileInfos", paginatedResult.paginatedFileInfos)
        // Есть ли еще результаты
        model.addAttribute("hasMoreResults", paginatedResult.hasMoreResults)
        // Номер текущей страницы
        model.addAttribute("page", paginatedResult.page)
        // Номера страниц для отображения
        model.addAttribute("pageNumbers", getPageNumbers(paginatedResult.page, paginatedResult.totalPages, 10))
        // Общее количество страниц
        model.addAttribute("totalPages", paginatedResult.totalPages)
        // Имя пользователя из авторизации
        model.addAttribute("nickname", authentication.name)

        // Добавление признака администратора
        if (authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            model.addAttribute("isAdmin", true)
        }
    }

    private fun setAttr(model: Model, attr: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            model.addAttribute(attr, value)
        }
    }

    private fun getPageNumbers(currentPage: Int, totalPages: Int, limit: Int): List<Int> {
        // Создаем пустой изменяемый список для хранения номеров страниц
        val pageNumbers: MutableList<Int> = ArrayList()

        // Если общее количество страниц меньше или равно лимиту
        if (totalPages <= limit) {
            // Добавляем все номера страниц от 0 до totalPages-1 в список
            for (i in 0 until totalPages) {
                pageNumbers.add(i)
            }
        } else {
            // Вычисляем начальную страницу, с которой начнется отображение
            // Берем максимальное значение между 0 и (текущая страница - половина лимита)
            var startPage = max(0.0, (currentPage - limit / 2).toDouble()).toInt()
            // Вычисляем конечную страницу, до которой будет идти отображение
            // Берем минимальное значение между (начальная страница + лимит) и totalPages
            val endPage = min((startPage + limit).toDouble(), totalPages.toDouble()).toInt()
            // Если разница между конечной и начальной страницей меньше лимита,
            // сдвигаем начальную страницу назад, чтобы заполнить лимит
            if (endPage - startPage < limit) {
                startPage = max(0.0, (endPage - limit).toDouble()).toInt()
            }
            // Если начальная страница больше 0, добавляем индикатор пропуска страниц
            if (startPage > 0) {
                pageNumbers.add(-1) // -1 как индикатор пропуска
            }
            // Добавляем номера страниц от начальной до конечной страницы в список
            for (i in startPage until endPage) {
                pageNumbers.add(i)
            }
            // Если конечная страница меньше общего количества страниц,
            // добавляем общее количество страниц как индикатор пропуска
            if (endPage < totalPages) {
                pageNumbers.add(totalPages) // Общее количество страниц как индикатор пропуска
            }
        }
        // Возвращаем сформированный список номеров страниц
        return pageNumbers
    }
}