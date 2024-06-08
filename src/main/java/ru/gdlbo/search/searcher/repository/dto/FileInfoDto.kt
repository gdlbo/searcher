package ru.gdlbo.search.searcher.repository.dto

data class FileInfoDto(
    val id: Long? = null,
    var decNumber: String? = null,
    var deviceName: String? = null,
    var documentType: String? = null,
    var usedDevices: String? = null,
    var project: String? = null,
    var lastModified: String? = null,
    var creationTime: String? = null,
    var inventoryNumber: String? = null,
    var location: String? = null,
    var username: String? = null
)