package ru.gdlbo.search.searcher.repository;

import lombok.Getter;

@Getter
public class FileInfo {
    private final String decNumber;
    private final String deviceName;
    private final String documentType;
    private final String usedDevices;
    private final String project;
    private final String inventoryNumber;
    private final String extension;
    private final String lastModified;
    private final String location;
    private final String creationTime;

    public FileInfo(String decNumber, String deviceName, String documentType, String usedDevices, String project, String inventoryNumber, String extension, String lastModified, String location, String creationTime) {
        this.decNumber = decNumber;
        this.deviceName = deviceName;
        this.documentType = documentType;
        this.usedDevices = usedDevices;
        this.project = project;
        this.inventoryNumber = inventoryNumber;
        this.extension = extension;
        this.lastModified = lastModified;
        this.location = location;
        this.creationTime = creationTime;
    }
}