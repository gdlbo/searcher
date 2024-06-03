package ru.gdlbo.search.searcher.repository;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FileTempInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String decNumber;
    private String deviceName;
    private String documentType;
    private String usedDevices;
    private String project;
    private String lastModified;
    private String creationTime;
    private String inventoryNumber;
    private String location;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FileTempInfo(String decNumber, String deviceName, String documentType, String usedDevices, String project, String inventoryNumber, String lastModified, String location, String creationTime, User user) {
        this.decNumber = decNumber;
        this.deviceName = deviceName;
        this.documentType = documentType;
        this.usedDevices = usedDevices;
        this.project = project;
        this.inventoryNumber = inventoryNumber;
        this.lastModified = lastModified;
        this.location = location;
        this.creationTime = creationTime;
        this.user = user;
    }

    public FileTempInfo(FileInfo fileInfo) {
        this.decNumber = fileInfo.getDecNumber();
        this.deviceName = fileInfo.getDeviceName();
        this.documentType = fileInfo.getDocumentType();
        this.usedDevices = fileInfo.getUsedDevices();
        this.project = fileInfo.getProject();
        this.lastModified = fileInfo.getLastModified();
        this.creationTime = fileInfo.getCreationTime();
        this.inventoryNumber = fileInfo.getInventoryNumber();
        this.location = fileInfo.getLocation();
        this.user = fileInfo.getUser();
    }

    public FileTempInfo(Long id, String decNumber, String deviceName, String documentType, String usedDevices, String project, String inventoryNumber, String lastModified, String location, String creationTime, User user) {
        this.id = id;
        this.decNumber = decNumber;
        this.deviceName = deviceName;
        this.documentType = documentType;
        this.usedDevices = usedDevices;
        this.project = project;
        this.inventoryNumber = inventoryNumber;
        this.lastModified = lastModified;
        this.location = location;
        this.creationTime = creationTime;
        this.user = user;
    }
}