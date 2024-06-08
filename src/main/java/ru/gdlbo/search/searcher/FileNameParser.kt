package ru.gdlbo.search.searcher

import java.time.LocalDate
import java.util.regex.Pattern

class FileNameParser(private val fileName: String) {
    fun extractDecNumber(): String {
        val decNumberPattern = "^[A-ZА-Я]+\\.[0-9]+\\.[0-9]+(?=(-[A-ZА-Я0-9]+)?|$)"
        val pattern = Pattern.compile(decNumberPattern)
        val matcher = pattern.matcher(fileName)

        return if (matcher.find()) {
            matcher.group(0).split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        } else {
            "N/A"
        }
    }

    fun extractDeviceName(): String {
        val parts = fileName.split("\\s|-|—".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size > 1) {
            var deviceName = parts[1].trim { it <= ' ' }
            val indexOfLastDot = deviceName.lastIndexOf('.')
            if (indexOfLastDot > 0) {
                deviceName = deviceName.substring(0, indexOfLastDot)
            }
            return deviceName
        } else {
            return "N/A"
        }
    }

    fun extractDocumentType(): String {
        val parts = fileName.split("\\s|-|—".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size > 2) {
            var documentType = parts[2].trim { it <= ' ' }
            val indexOfLastDot = documentType.lastIndexOf('.')
            if (indexOfLastDot > 0) {
                documentType = documentType.substring(0, indexOfLastDot)
            }
            return documentType
        } else {
            return "N/A"
        }
    }

    fun extractUsedDevices(): String {
        val parts = fileName.split("—|\\s|-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size > 3) parts[3].trim { it <= ' ' } else "N/A"
    }

    fun extractProject(): String {
        val parts = fileName.split("—|\\s|-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size > 4) parts[4].trim { it <= ' ' } else "N/A"
    }

    fun extractInventoryNumber(userHasExtendedRights: Boolean): String {
        val inventoryNumberPattern = "[0-9]{2}/[0-9]{2}-[0-9]{3}"
        val pattern = Pattern.compile(inventoryNumberPattern)
        val matcher = pattern.matcher(fileName)

        return if (matcher.find()) {
            matcher.group(0)
        } else {
            if (userHasExtendedRights) {
                generateInventoryNumber()
            } else {
                "N/A"
            }
        }
    }

    private fun generateInventoryNumber(): String {
        val month = LocalDate.now().monthValue
        val year = LocalDate.now().year % 100
        currentDrawingNumber++
        return String.format("%02d/%02d-%03d", month, year, currentDrawingNumber)
    }

    fun extractExtension(): String {
        val lastIndexOfDot = fileName.lastIndexOf(".")
        if (lastIndexOfDot == -1 || lastIndexOfDot == 0 || lastIndexOfDot == fileName.length - 1) {
            return "none"
        }
        return fileName.substring(lastIndexOfDot + 1)
    }

    companion object {
        private var currentDrawingNumber = 0 // Текущий номер чертежа для автоматического присвоения
    }
}