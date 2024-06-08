package ru.gdlbo.search.searcher.controllers

import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.io.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller
class FileDownloadController {
    // This method is responsible for handling file download requests
    @GetMapping("/api/download")
    @Throws(IOException::class)
    fun downloadFile(@RequestParam filePath: String): ResponseEntity<InputStreamResource> {
        // Creating a new File object from the provided filePath

        val file = File(filePath)
        println("File object created with path: $filePath")

        // Creating an InputStreamResource to read from the file
        val resource = InputStreamResource(FileInputStream(file))
        println("InputStreamResource created to read from the file")

        // Getting the name of the file
        val fileName = file.name
        println("File name: $fileName")

        // Encoding the file name to ensure it's properly displayed in all browsers
        val encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        println("Encoded file name: $encodedFileName")

        // Building and returning the ResponseEntity with the file as the body
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''$encodedFileName")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }
}