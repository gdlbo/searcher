package ru.gdlbo.search.searcher.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.gdlbo.search.searcher.config.Config
import ru.gdlbo.search.searcher.repository.FileInfo
import ru.gdlbo.search.searcher.repository.FileInfoSpecification
import ru.gdlbo.search.searcher.repository.FileTempInfo
import ru.gdlbo.search.searcher.repository.PaginatedResult
import ru.gdlbo.search.searcher.services.FileService
import ru.gdlbo.search.searcher.services.UserService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Controller
class FileSearchController {
    @Autowired
    private val fileService: FileService? = null

    @Autowired
    private val userService: UserService? = null

    @Autowired
    private val config: Config? = null

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

        val fileInfos = fileService!!.findFiles(spec)
        val paginatedResult = fileService.paginateFileInfos(fileInfos, page)

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

    @GetMapping("/api/setDummyFiles")
    fun setDummyFiles(): ResponseEntity<String> {
        createDummyFiles(500)
        return ResponseEntity.ok("Dummy files created successfully")
    }

    @GetMapping("/api/searchFile")
    fun searchFiles(@RequestParam id: Long): ResponseEntity<FileInfo> {
        return fileService!!.getFileById(id)
            .map<ResponseEntity<FileInfo>> { body: FileInfo? -> ResponseEntity.ok(body) }
            .orElseGet { ResponseEntity.status(HttpStatus.NOT_FOUND).build() }
    }

    @GetMapping("/api/searchTempFile")
    fun searchTempFiles(@RequestParam id: Long): ResponseEntity<FileTempInfo> {
        return fileService!!.getTempFileById(id)
            .map<ResponseEntity<FileTempInfo>> { body: FileTempInfo? -> ResponseEntity.ok(body) }
            .orElseGet { ResponseEntity.status(HttpStatus.NOT_FOUND).build() }
    }

    fun createDummyFiles(count: Int) {
        val defaultUser = userService!!.findByUsername("admin")
        val random = Random()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        for (i in 1..count) {
            val randomNumber = random.nextInt(1000)
            val fileName = String.format("ВГМТ.%06d.%03d МАРШРУТИЗАТОР INCARNET %d.pdf", 465245 + i, 7, randomNumber)

            val randomDateTime = LocalDateTime.now().plusDays(random.nextInt(365).toLong())
            val formattedDateTime = randomDateTime.format(formatter)

            val dummyFile = FileInfo(
                fileName,
                "Device $i",
                "Type $i",
                "Used $i",
                "Project $i",
                i.toString(),
                formattedDateTime,
                "/path/to/location",
                formattedDateTime,
                defaultUser!!
            )
            fileService!!.saveOrUpdateFile(dummyFile)
        }
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

    fun getPageNumbers(currentPage: Int, totalPages: Int, limit: Int): List<Int> {
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