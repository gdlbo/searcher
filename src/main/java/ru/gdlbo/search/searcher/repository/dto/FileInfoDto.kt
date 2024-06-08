package ru.gdlbo.search.searcher.repository.dto

data class FileInfoDto(
    val id: Long?,
    val decNumber: String,
    val deviceName: String,
    val documentType: String,
    val usedDevices: String,
    val project: String,
    val lastModified: String,
    val creationTime: String,
    val inventoryNumber: String,
    val location: String,
    val userName: String
)