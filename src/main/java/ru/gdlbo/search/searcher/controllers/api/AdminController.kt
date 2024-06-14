package ru.gdlbo.search.searcher.controllers.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.gdlbo.search.searcher.config.RestartManager
import ru.gdlbo.search.searcher.repository.*
import ru.gdlbo.search.searcher.repository.files.FileInfo
import ru.gdlbo.search.searcher.repository.files.FileInfoRepository
import ru.gdlbo.search.searcher.repository.files.FileTempInfo
import ru.gdlbo.search.searcher.repository.files.FileTempInfoRepository
import ru.gdlbo.search.searcher.repository.role.Role
import ru.gdlbo.search.searcher.repository.role.RoleRepository
import ru.gdlbo.search.searcher.repository.user.User
import ru.gdlbo.search.searcher.repository.user.UserRepository
import ru.gdlbo.search.searcher.repository.user.UserRole
import java.io.*
import java.nio.file.Paths
import java.util.*

@RestController
class AdminController {
    @Autowired
    private val restartManager: RestartManager? = null // Менеджер перезапуска приложения

    @Autowired
    private val userRepository: UserRepository? = null // Репозиторий для работы с пользователями

    @Autowired
    private val fileInfoRepository: FileInfoRepository? = null // Репозиторий для работы с информацией о файлах

    @Autowired
    private val fileTempInfoRepository: FileTempInfoRepository? = null // Репозиторий для работы с информацией о временных файлах

    @Autowired
    private val roleRepository: RoleRepository? = null // Репозиторий для работы с ролями

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null // Сервис для шифрования паролей

    @GetMapping("/api/grantAdmin")
    fun grantAdminPrivileges(
        @RequestParam username: String,
        authentication: Authentication
    ): ResponseEntity<String> {
        // Проверка прав администратора у текущего пользователя
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity("Пользователь не является администратором", HttpStatus.FORBIDDEN)
        }

        val userOptional: Optional<User> = userRepository!!.findByUsername(username) // Поиск пользователя по имени

        return userOptional.map { user -> // Обработать найденного пользователя
            val adminRole = roleRepository!!.findByName("ROLE_ADMIN") // Получить роль администратора
                .orElseGet { // Если роль не найдена, создать новую
                    val newRole = Role("ROLE_ADMIN")
                    roleRepository.save(newRole)
                    newRole
                }
            if (user.userRoles.stream().anyMatch { userRole: UserRole -> userRole.role.name == "ROLE_ADMIN" }) { // Проверка наличия роли администратора у пользователя
                ResponseEntity.ok("Пользователь уже является администратором")
            } else {
                user.userRoles.add(UserRole(user, adminRole)) // Добавление роли администратора пользователю
                userRepository.save(user)
                ResponseEntity.ok("Пользователю $username предоставлены права администратора")
            }
        }.orElseGet { ResponseEntity("Пользователь не найден", HttpStatus.NOT_FOUND) } // Обработать случай, когда пользователь не найден
    }

    @GetMapping("/api/resetDatabase")
    fun resetDatabase(authentication: Authentication): ResponseEntity<String> {
        // Проверка прав администратора у текущего пользователя
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Пользователь не является администратором")
        }

        val thread = Thread { // Создание потока для сброса базы данных
            userRepository!!.deleteAll() // Удалить всех пользователей
            restartManager!!.restart() // Перезапустить приложение
            fileInfoRepository!!.deleteAll() // Удалить всю информацию о файлах
        }
        thread.isDaemon = false
        thread.start()

        return ResponseEntity.ok("База данных сброшена")
    }

    @GetMapping("/api/resetFileDatabase")
    fun resetFileDatabase(authentication: Authentication): ResponseEntity<String> {
        // Проверка прав администратора у текущего пользователя
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity("Пользователь не является администратором", HttpStatus.FORBIDDEN)
        }

        val thread = Thread { // Создание потока для сброса таблиц с информацией о файлах
            fileInfoRepository!!.deleteAll() // Удалить всю информацию о файлах
            fileTempInfoRepository!!.deleteAll() // Удалить всю информацию о временных файлах
        }
        thread.isDaemon = false
        thread.start()

        return ResponseEntity.ok("База данных с информацией о файлах сброшена")
    }

    @GetMapping("/api/restartServer")
    fun restartServer(authentication: Authentication): ResponseEntity<String> {
        // Проверка прав администратора у текущего пользователя
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity("Пользователь не является администратором", HttpStatus.FORBIDDEN)
        }

        val thread = Thread { // Создание потока для перезапуска сервера
            restartManager!!.restart()
        }
        thread.isDaemon = false
        thread.start()

        return ResponseEntity.ok("Сервер перезапускается...")
    }
    @GetMapping("/api/changeCredentials")
    fun changeCredentials(
        @RequestParam(required = false) username: String?,
        @RequestParam oldPassword: String?,
        @RequestParam(required = false) newPassword: String?,
        authentication: Authentication
    ): ResponseEntity<String> {
        // Получение текущего пользователя
        val currentUser = userRepository!!.findByUsername(authentication.name).orElse(null)
            ?: return ResponseEntity("Текущий пользователь не найден", HttpStatus.BAD_REQUEST)

        // Проверка совпадения со старым паролем
        if (!passwordEncoder!!.matches(oldPassword, currentUser.password)) {
            return ResponseEntity("Старый пароль неверный", HttpStatus.BAD_REQUEST)
        }

        // Изменение имени пользователя
        if (!username.isNullOrEmpty() && username != currentUser.username) {
            val exists = userRepository.findByUsername(username).isPresent
            if (exists) {
                return ResponseEntity("Пользователь с таким именем уже существует", HttpStatus.BAD_REQUEST)
            }
            currentUser.username = username
        }

        // Изменение пароля
        if (!newPassword.isNullOrEmpty()) {
            currentUser.password = passwordEncoder.encode(newPassword)
        }

        // Сохранение изменений в базе данных
        userRepository.save(currentUser)

        return ResponseEntity.ok("Данные пользователя успешно обновлены")
    }

    @GetMapping("/api/removeFile")
    fun removeFile(@RequestParam id: Long): ResponseEntity<String> {
        println("Получен запрос на удаление файла: $id")

        fileInfoRepository!!.findById(id).ifPresent { file: FileInfo -> // Поиск файла по id
            val fileToDelete = File(file.location.toString()) // Получение объекта File для удаляемого файла

            // Удаление истории файла
            FileHistoryController().deleteFileHistory(fileToDelete.absolutePath)

            // Удаление файла, если он существует
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }

        fileInfoRepository.deleteById(id) // Удаление информации о файле из базы данных
        return ResponseEntity.ok("Файл успешно удален")
    }

    @GetMapping("/api/removeTempFile")
    fun removeTempFile(@RequestParam id: Long, authentication: Authentication): ResponseEntity<String> {
        fileTempInfoRepository!!.findById(id).ifPresent { file: FileTempInfo -> // Поиск временного файла по id
            val fileToDelete = file.location?.let { File(it) } // Получение объекта File для удаляемого временного файла

            // Проверка прав на удаление временного файла
            if (authentication.name == file.user?.username || authentication.authorities.contains(
                    SimpleGrantedAuthority(
                        "ROLE_ADMIN"
                    )
                )
            ) {
                if (fileToDelete?.exists() == true) { // Удаление временного файла, если он существует
                    fileToDelete.delete()
                }

                fileTempInfoRepository.delete(file) // Удаление информации о временном файле из базы данных
            }
        }
        return ResponseEntity.ok("Временный файл успешно удален")
    }

    @GetMapping("/api/submitCustomPath")
    @Throws(IOException::class)
    fun submitCustomPath(@RequestParam searcherPath: String?): ResponseEntity<String> {
        var searcherPath = searcherPath // Получение пути поиска из запроса
        val propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties") // Путь к файлу application.properties

        // Проверка существования файла application.properties
        if (!propertiesPath.toFile().exists()) {
            propertiesPath.toFile().createNewFile() // Создать файл, если он не существует
        }

        val properties = Properties() // Создание объекта Properties

        FileInputStream(propertiesPath.toFile()).use { inStream -> // Чтение содержимого файла application.properties
            properties.load(inStream)
        }

        // Получение пути поиска из файла application.properties, если он не указан в запросе
        if (searcherPath.isNullOrEmpty()) {
            searcherPath = properties.getProperty("searcher.path")
        }

        // Запись нового пути поиска в файл application.properties
        properties.setProperty("searcher.path", searcherPath)

        FileOutputStream(propertiesPath.toFile()).use { outStream -> // Запись изменений в файл настроек application.properties
            properties.store(outStream, null)
        }

        // Перезапуск приложения после изменения пути поиска
        restartManager!!.restart()

        return ResponseEntity.ok("Перезапуск...")
    }

    @GetMapping("/api/dropCustomPath")
    fun dropCustomPath(): ResponseEntity<String> {
        val propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties") // Путь к файлу application.properties

        // Удаление файла application.properties
        propertiesPath.toFile().delete()

        restartManager!!.restart() // Перезапуск сервера для применения настроек

        return ResponseEntity.ok("Перезапуск...")
    }
}