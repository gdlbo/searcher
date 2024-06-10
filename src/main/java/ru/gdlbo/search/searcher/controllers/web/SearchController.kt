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
    private val fileService: FileService? = null

    @Autowired
    private val userService: UserService? = null

    @Autowired
    private val config: Config? = null

    @PostMapping("/api/web/replaceFile")
    @Throws(Exception::class)
    fun replaceTempFile(
        @RequestParam filePath: String,
        @RequestParam file: MultipartFile,
        authentication: Authentication
    ): String {
        println("Request received to replace file: $filePath")

        if (file.isEmpty) {
            return "redirect:/error"
        }

        val oldFile = File(filePath)

        val hiddenDir = createHiddenDirectory(oldFile)
        checkAndDeleteOldVersions(hiddenDir, oldFile.name)

        replaceOldFileWithNew(file, filePath)

        return "redirect:/search"
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

    @GetMapping("/search")
    fun searchFiles(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam formData: Map<String?, String?>,
        authentication: Authentication,
        model: Model
    ): String {
        val isAdmin = authentication.authorities.contains(SimpleGrantedAuthority("ROLE_ADMIN"))
        val hasCustomPath = config?.path != null
        val decNumber = formData["decNumber"]
        val deviceName = formData["deviceName"]
        val documentType = formData["documentType"]
        val usedDevices = formData["usedDevices"]
        val project = formData["project"]
        val inventoryNumber = formData["inventoryNumber"]
        val lastModified = formData["lastModified"]
        val location = formData["location"]
        val creationTime = formData["creationTime"]
        val userName = if (isAdmin) formData["user"] else authentication.name

        userService!!.findByUsername(authentication.name) ?: return "redirect:/error"

        val spec = FileInfoSpecification.createSpecification(
            decNumber, deviceName, documentType, usedDevices, project,
            inventoryNumber, lastModified, location, creationTime, userName
        )

        val files = fileService!!.findFiles(spec)
        val paginatedResult = fileService.paginateFileInfos(files, page)

        addAttributesToModel(model, paginatedResult, authentication)

        setAttr(model, "decNumber", decNumber)
        setAttr(model, "deviceName", deviceName)
        setAttr(model, "documentType", documentType)
        setAttr(model, "usedDevices", usedDevices)
        setAttr(model, "project", project)
        setAttr(model, "inventoryNumber", inventoryNumber)
        setAttr(model, "creationTime", creationTime)
        setAttr(model, "lastModified", lastModified)
        setAttr(model, "location", location)

        model.addAttribute("isCustomPathDisabled", !hasCustomPath)

        if (hasCustomPath && isAdmin) {
            model.addAttribute("path", config?.path)
        }

        return "search"
    }

    private fun addAttributesToModel(model: Model, paginatedResult: PaginatedResult, authentication: Authentication) {
        model.addAttribute("fileInfos", paginatedResult.paginatedFileInfos)
        model.addAttribute("hasMoreResults", paginatedResult.hasMoreResults)
        model.addAttribute("page", paginatedResult.page)
        model.addAttribute("pageNumbers", getPageNumbers(paginatedResult.page, paginatedResult.totalPages, 10))
        model.addAttribute("totalPages", paginatedResult.totalPages)
        model.addAttribute("nickname", authentication.name)

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
        val pageNumbers: MutableList<Int> = ArrayList()

        if (totalPages <= limit) {
            for (i in 0 until totalPages) {
                pageNumbers.add(i)
            }
        } else {
            var startPage = max(0.0, (currentPage - limit / 2).toDouble()).toInt()
            val endPage = min((startPage + limit).toDouble(), totalPages.toDouble()).toInt()

            if (endPage - startPage < limit) {
                startPage = max(0.0, (endPage - limit).toDouble()).toInt()
            }

            if (startPage > 0) {
                pageNumbers.add(-1)
            }

            for (i in startPage until endPage) {
                pageNumbers.add(i)
            }

            if (endPage < totalPages) {
                pageNumbers.add(totalPages)
            }
        }

        return pageNumbers
    }
}