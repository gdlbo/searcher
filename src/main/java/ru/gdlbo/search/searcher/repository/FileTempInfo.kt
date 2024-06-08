package ru.gdlbo.search.searcher.repository

import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import ru.gdlbo.search.searcher.repository.dto.FileInfoDto

@Entity
@Getter
@Setter
@NoArgsConstructor
data class FileTempInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
) {
    constructor(
        decNumber: String,
        deviceName: String,
        documentType: String,
        usedDevices: String,
        project: String,
        inventoryNumber: String,
        lastModified: String,
        location: String,
        creationTime: String,
        user: User
    ) : this(
        id = null,
        decNumber = decNumber,
        deviceName = deviceName,
        documentType = documentType,
        usedDevices = usedDevices,
        project = project,
        lastModified = lastModified,
        creationTime = creationTime,
        inventoryNumber = inventoryNumber,
        location = location,
        user = user
    )

    constructor(fileInfo: FileInfo) : this(
        id = null,
        decNumber = fileInfo.decNumber,
        deviceName = fileInfo.deviceName,
        documentType = fileInfo.documentType,
        usedDevices = fileInfo.usedDevices,
        project = fileInfo.project,
        lastModified = fileInfo.lastModified,
        creationTime = fileInfo.creationTime,
        inventoryNumber = fileInfo.inventoryNumber,
        location = fileInfo.location,
        user = fileInfo.user
    )

    fun toDTO() = FileInfoDto(
        id = this.id,
        decNumber = this.decNumber,
        deviceName = this.deviceName,
        documentType = this.documentType,
        usedDevices = this.usedDevices,
        project = this.project,
        lastModified = this.lastModified,
        creationTime = this.creationTime,
        inventoryNumber = this.inventoryNumber,
        location = this.location,
        username = this.user?.username
    )
}