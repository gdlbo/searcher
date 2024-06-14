package ru.gdlbo.search.searcher.controllers.api

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
    // Этот метод обрабатывает запросы на скачивание файлов
    @GetMapping("/api/download")
    @Throws(IOException::class)
    fun downloadFile(@RequestParam filePath: String): ResponseEntity<InputStreamResource> {
        // Создание объекта из пути к файлу
        val file = File(filePath)
        println("Создан файл с путем: $filePath")

        // Создание потока для чтения из файла
        val resource = InputStreamResource(FileInputStream(file))
        println("Создан InputStreamResource для чтения из файла")

        // Получение имени файла
        val fileName = file.name
        println("Имя файла: $fileName")

        // Кодировка имени файла для корректного отображения в браузерах
        val encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        println("Закодированное имя файла: $encodedFileName")

        // Создание и возврат ResponseEntity с файлом в теле ответа для начала загрузки
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''$encodedFileName")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }
}