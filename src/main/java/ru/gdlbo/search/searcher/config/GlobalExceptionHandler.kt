package ru.gdlbo.search.searcher.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResponseEntity<Map<String, String>> {
        val response: MutableMap<String, String> = HashMap()
        response["message"] = "Required request parameter '" + ex.parameterName + "' is not present"
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
}