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
    private val fileService: FileService? = null

    @Autowired
    private val userService: UserService? = null

    @GetMapping("review")
    fun review(model: Model, auth: Authentication): String {
        val fileInfos: List<FileInfoDto?>?

        if (!auth.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))) {
            val user = userService!!.findByUsername(auth.name)
            fileInfos = fileService!!.findTempByUser(user)?.map { it?.toDTO() }
        } else {
            fileInfos = fileService!!.findAllTemp().map { it?.toDTO() }
            model.addAttribute("isAdmin", true)
        }

        model.addAttribute("fileInfos", fileInfos)
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
            return "redirect:/error"
        }

        val fileTempInfo = fileService?.getTempFileById(fileId)

        if (fileTempInfo != null && fileTempInfo.isPresent) {
            val fileTempInfo = fileTempInfo.get()
            if (fileTempInfo.user?.username == authentication.name || authentication.authorities.contains(
                    SimpleGrantedAuthority("ROLE_ADMIN")
                )
            ) {
                FileCopyUtils.copy(file.inputStream.readAllBytes(), File(fileTempInfo.location.toString()))
                return "redirect:/review"
            } else {
                println("Trying to replace file without permission")
                return "redirect:/error"
            }
        } else {
            println("File isn't exist")
            return "redirect:/error"
        }
    }
}