package ru.gdlbo.search.searcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameParser {
    private final String fileName;
    private static int currentDrawingNumber = 0; // Текущий номер чертежа для автоматического присвоения

    public FileNameParser(String fileName) {
        this.fileName = fileName;
    }

    public String extractDecNumber() {
        String decNumberPattern = "^[A-ZА-Я]+\\.[0-9]+\\.[0-9]+(?=(-[A-ZА-Я0-9]+)?|$)";
        Pattern pattern = Pattern.compile(decNumberPattern);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(0).split("-")[0];
        } else {
            return "N/A";
        }
    }

    public String extractDeviceName() {
        String[] parts = fileName.split("\\s|-|—");
        if (parts.length > 1) {
            String deviceName = parts[1].trim();
            int indexOfLastDot = deviceName.lastIndexOf('.');
            if (indexOfLastDot > 0) {
                deviceName = deviceName.substring(0, indexOfLastDot);
            }
            return deviceName;
        } else {
            return "N/A";
        }
    }

    public String extractDocumentType() {
        String[] parts = fileName.split("\\s|-|—");
        if (parts.length > 2) {
            String documentType = parts[2].trim();
            int indexOfLastDot = documentType.lastIndexOf('.');
            if (indexOfLastDot > 0) {
                documentType = documentType.substring(0, indexOfLastDot);
            }
            return documentType;
        } else {
            return "N/A";
        }
    }

    public String extractUsedDevices() {
        String[] parts = fileName.split("—|\\s|-");
        return parts.length > 3 ? parts[3].trim() : "N/A";
    }

    public String extractProject() {
        String[] parts = fileName.split("—|\\s|-");
        return parts.length > 4 ? parts[4].trim() : "N/A";
    }

    public String extractInventoryNumber(boolean userHasExtendedRights) {
        String inventoryNumberPattern = "[0-9]{2}/[0-9]{2}-[0-9]{3}";
        Pattern pattern = Pattern.compile(inventoryNumberPattern);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            if (userHasExtendedRights) {
                return generateInventoryNumber();
            } else {
                return "N/A";
            }
        }
    }

    private String generateInventoryNumber() {
        int month = java.time.LocalDate.now().getMonthValue();
        int year = java.time.LocalDate.now().getYear() % 100;
        currentDrawingNumber++;
        return String.format("%02d/%02d-%03d", month, year, currentDrawingNumber);
    }

    public String extractExtension() {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1 || lastIndexOfDot == 0 || lastIndexOfDot == fileName.length() - 1) {
            return "none";
        }
        return fileName.substring(lastIndexOfDot + 1);
    }
}