package ru.gdlbo.search.searcher.controllers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import ru.gdlbo.search.searcher.config.Config
import ru.gdlbo.search.searcher.repository.*
import ru.gdlbo.search.searcher.services.FileService
import ru.gdlbo.search.searcher.services.UserService
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
class FileUploadController {
    @Autowired
    private val fileService: FileService? = null

    @Autowired
    private val userService: UserService? = null

    @Autowired
    private val config: Config? = null

    @PostMapping("/api/upload")
    @ResponseBody
    @Throws(Exception::class)
    fun uploadFile(
        @RequestParam decNumber: String,
        @RequestParam deviceName: String,
        @RequestParam documentType: String,
        @RequestParam(required = false) usedDevices: String?,
        @RequestParam project: String,
        @RequestParam inventoryNumber: String,
        @RequestParam(required = false) location: String,
        @RequestParam file: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        var usedDevices = usedDevices
        var location = location
        val response: MutableMap<String, String> = HashMap()
        val user = userService!!.findByUsername(authentication.name)
        if (user == null) {
            response["error"] = "Ошибка: Не удалось найти пользователя " + authentication.name
            return ResponseEntity.badRequest().body(response)
        }

        if (file.isEmpty) {
            response["error"] = "Файл не может быть пустым"
            return ResponseEntity.badRequest().body(response)
        }

        if (fileService!!.existsByDecNumber(decNumber)) {
            response["error"] = "Такой децимальный номер уже существует"
            return ResponseEntity.badRequest().body(response)
        }

        if (config?.path.isNullOrBlank()) {
            location = config?.path.toString()
        }

        if (usedDevices == null || usedDevices.isEmpty()) {
            usedDevices = "N/A"
        }

        val creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val lastModified = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val locationWithFileName: String

        if (isAdmin(authentication)) {
            locationWithFileName = location + "/" + file.originalFilename
        } else {
            val targetDirectory = createHiddenDirectory(File(location))
            locationWithFileName = targetDirectory.path + "/" + file.originalFilename
        }

        val fileInfo = FileInfo(
            decNumber,
            deviceName,
            documentType,
            usedDevices,
            project,
            inventoryNumber,
            lastModified,
            locationWithFileName,
            creationTime,
            user
        )

        if (isAdmin(authentication)) {
            fileService.saveOrUpdateFile(fileInfo)
        } else {
            fileService.saveTempFile(FileTempInfo(fileInfo))
        }

        val newFile = File(locationWithFileName)
        FileCopyUtils.copy(file.inputStream.readAllBytes(), newFile)

        println("Uploaded file: $locationWithFileName")

        response["success"] = "Файл успешно загружен"
        return ResponseEntity.ok(response)
    }

    @Throws(Exception::class)
    private fun createHiddenDirectory(oldFile: File): File {
        val hiddenDir = File(oldFile, ".review")
        if (!hiddenDir.exists()) {
            if (!hiddenDir.mkdir()) {
                throw Exception("Failed to create directory")
            }
        }
        return hiddenDir
    }

    @PostMapping("/api/update")
    fun updateFile(
        @RequestParam id: Long,
        @RequestParam decNumber: String,
        @RequestParam deviceName: String,
        @RequestParam documentType: String,
        @RequestParam usedDevices: String,
        @RequestParam project: String,
        @RequestParam lastModified: String,
        @RequestParam creationTime: String,
        @RequestParam inventoryNumber: String,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) userName: String,
        @RequestParam(defaultValue = "false", required = false) isReview: Boolean,
        request: HttpServletRequest,
        authentication: Authentication
    ): String {
        var location = location
        var userName = userName
        if (!isReview && !isAdmin(authentication)) {
            return "redirect:/error"
        }

        val optionalFileInfo = fileService!!.getFileById(id)
        val optionalTempFileInfo = fileService.getTempFileById(id)
        val referer = request.getHeader("Referer")

        if (optionalTempFileInfo.isPresent || optionalFileInfo.isPresent) {
            if (isReview) {
                location = optionalTempFileInfo.get().location
                userName = authentication.name
            }

            val user = userService!!.findByUsername(userName)

            if (user == null || (user.username != userName && isReview && !isAdmin(authentication))) {
                println("Failed to find user with username: $userName")
                return "redirect:/error"
            }

            val formattedLastModified = formatDateTime(lastModified)
            val formattedCreationTime = formatDateTime(creationTime)

            if (isReview) {
                saveTempFile(
                    id,
                    decNumber,
                    deviceName,
                    documentType,
                    usedDevices,
                    project,
                    inventoryNumber,
                    formattedLastModified,
                    location,
                    formattedCreationTime,
                    user
                )
            } else {
                saveFile(
                    id,
                    decNumber,
                    deviceName,
                    documentType,
                    usedDevices,
                    project,
                    inventoryNumber,
                    formattedLastModified,
                    location,
                    formattedCreationTime,
                    user
                )
            }
            return "redirect:$referer"
        } else {
            println("Failed to find file with id: $id")
            return "redirect:/error"
        }
    }

    private fun isAdmin(authentication: Authentication): Boolean {
        return authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))
    }

    private fun saveTempFile(
        id: Long,
        decNumber: String,
        deviceName: String,
        documentType: String,
        usedDevices: String,
        project: String,
        inventoryNumber: String,
        formattedLastModified: String,
        location: String?,
        formattedCreationTime: String,
        user: User
    ) {
        val fileInfo = FileTempInfo(
            id,
            decNumber,
            deviceName,
            documentType,
            usedDevices,
            project,
            inventoryNumber,
            formattedLastModified,
            location,
            formattedCreationTime,
            user
        )
        fileService!!.saveTempFile(fileInfo)
    }

    private fun saveFile(
        id: Long,
        decNumber: String,
        deviceName: String,
        documentType: String,
        usedDevices: String,
        project: String,
        inventoryNumber: String,
        formattedLastModified: String,
        location: String?,
        formattedCreationTime: String,
        user: User
    ) {
        val fileInfo = FileInfo(
            id,
            decNumber,
            deviceName,
            documentType,
            usedDevices,
            project,
            inventoryNumber,
            formattedLastModified,
            location,
            formattedCreationTime,
            user
        )
        fileService!!.saveOrUpdateFile(fileInfo)
    }

    private fun formatDateTime(dateTime: String): String {
        val localDateTime = LocalDateTime.parse(dateTime)
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
}