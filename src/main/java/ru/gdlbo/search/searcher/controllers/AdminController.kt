package ru.gdlbo.search.searcher.controllers

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
import java.io.*
import java.nio.file.Paths
import java.util.*

@RestController
class AdminController {
    @Autowired
    private val restartManager: RestartManager? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val fileInfoRepository: FileInfoRepository? = null

    @Autowired
    private val fileTempInfoRepository: FileTempInfoRepository? = null

    @Autowired
    private val roleRepository: RoleRepository? = null

    @Autowired
    private val passwordEncoder: PasswordEncoder? = null

    @GetMapping("/api/grantAdmin")
    fun grantAdminPrivileges(@RequestParam username: String, authentication: Authentication): ResponseEntity<String> {
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin")
        }

        val userOptional: Optional<User> = userRepository!!.findByUsername(username)

        return userOptional.map { user ->
            val adminRole = roleRepository!!.findByName("ROLE_ADMIN")
                .orElseGet {
                    val newRole = Role("ROLE_ADMIN")
                    roleRepository.save(newRole)
                    newRole
                }
            if (user.userRoles.stream().anyMatch { userRole: UserRole -> userRole.role.name == "ROLE_ADMIN" }) {
                ResponseEntity.ok("User already an admin")
            } else {
                user.userRoles.add(UserRole(user, adminRole))
                userRepository.save(user)
                ResponseEntity.ok("Admin privileges granted to $username")
            }
        }.orElseGet { ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist") }
    }

    @GetMapping("/api/resetDatabase")
    fun resetDatabase(authentication: Authentication): ResponseEntity<String> {
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin")
        }

        val thread = Thread {
            userRepository!!.deleteAll()
            restartManager!!.restart()
            fileInfoRepository!!.deleteAll()
        }
        thread.isDaemon = false
        thread.start()

        return ResponseEntity.ok("Database has been reset")
    }

    @GetMapping("/api/resetFileDatabase")
    fun resetFileDatabase(authentication: Authentication): ResponseEntity<String> {
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin")
        }

        val thread = Thread {
            fileInfoRepository!!.deleteAll()
            fileTempInfoRepository!!.deleteAll()
        }
        thread.isDaemon = false
        thread.start()

        return ResponseEntity.ok("Database has been reset")
    }

    @GetMapping("/api/restartServer")
    fun restartServer(authentication: Authentication): ResponseEntity<String> {
        if (!authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not an admin")
        }

        val thread = Thread {
            restartManager!!.restart()
        }
        thread.isDaemon = false
        thread.start()

        return ResponseEntity.ok("Server is restarting...")
    }

    @GetMapping("/api/changeCredentials")
    fun changeCredentials(
        @RequestParam(required = false) username: String?,
        @RequestParam oldPassword: String?,
        @RequestParam(required = false) newPassword: String?,
        authentication: Authentication
    ): ResponseEntity<String> {
        val currentUser = userRepository!!.findByUsername(authentication.name).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Current user not found")

        if (!passwordEncoder!!.matches(oldPassword, currentUser.password)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect")
        }

        if (!username.isNullOrEmpty() && username != currentUser.username) {
            val exists = userRepository.findByUsername(username).isPresent
            if (exists) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this username already exists")
            }
            currentUser.username = username
        }

        if (!newPassword.isNullOrEmpty()) {
            currentUser.password = passwordEncoder.encode(newPassword)
        }

        userRepository.save(currentUser)

        return ResponseEntity.ok("Credentials updated successfully")
    }

    @GetMapping("/api/removeFile")
    fun removeFile(@RequestParam id: Long): String {
        println("Request received to remove file: $id")

        fileInfoRepository!!.findById(id).ifPresent { file: FileInfo ->
            val fileToDelete = File(file.location)
            FileHistoryController().deleteFileHistory(fileToDelete.absolutePath)
            if (fileToDelete.exists()) {
                fileToDelete.delete()
            }
        }

        fileInfoRepository.deleteById(id)
        return "redirect:/search"
    }

    @GetMapping("/api/removeTempFile")
    fun removeTempFile(@RequestParam id: Long, authentication: Authentication): String {
        fileTempInfoRepository!!.findById(id).ifPresent { file: FileTempInfo ->
            val fileToDelete = file.location?.let { File(it) }
            if (authentication.name == file.user?.username || authentication.authorities.contains(
                    SimpleGrantedAuthority(
                        "ROLE_ADMIN"
                    )
                )
            ) {
                if (fileToDelete?.exists() == true) {
                    fileToDelete.delete()
                }

                fileTempInfoRepository.delete(file)
            }
        }
        return "redirect:/review"
    }

    @GetMapping("/api/submitCustomPath")
    @Throws(IOException::class)
    fun submitCustomPath(@RequestParam searcherPath: String?): ResponseEntity<String> {
        var searcherPath = searcherPath
        val propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties")

        if (!propertiesPath.toFile().exists()) {
            propertiesPath.toFile().createNewFile()
        }

        val properties = Properties()

        FileInputStream(propertiesPath.toFile()).use { inStream ->
            properties.load(inStream)
        }
        if (searcherPath == null || searcherPath.isEmpty()) {
            searcherPath = properties.getProperty("searcher.path")
        }

        properties.setProperty("searcher.path", searcherPath)

        FileOutputStream(propertiesPath.toFile()).use { outStream ->
            properties.store(outStream, null)
        }
        restartManager!!.restart()

        return ResponseEntity.ok("Restarting...")
    }

    @GetMapping("/api/dropCustomPath")
    fun dropCustomPath(): ResponseEntity<String> {
        val propertiesPath = Paths.get(System.getProperty("user.dir"), "application.properties")

        propertiesPath.toFile().delete()

        restartManager!!.restart()

        return ResponseEntity.ok("Restarting...")
    }
}