package ru.gdlbo.search.searcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileNameParser {
    private final String fileName;

    public FileNameParser(String fileName) {
        this.fileName = fileName;
    }

    public String extractDecNumber() {
        String decNumberPattern = "^[A-ZА-Я]+\\.[0-9]+\\.[0-9]+(-[A-ZА-Я0-9]+)?";
        Pattern pattern = Pattern.compile(decNumberPattern);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return "N/A";
        }
    }

    public String extractDeviceName() {
        String[] parts = fileName.split("—|\\s|-");
        return parts.length > 1 ? parts[1].trim() : "N/A";
    }

    public String extractDocumentType() {
        String[] parts = fileName.split("—|\\s|-");
        return parts.length > 2 ? parts[2].trim() : "N/A";
    }

    public String extractUsedDevices() {
        String[] parts = fileName.split("—|\\s|-");
        return parts.length > 3 ? parts[3].trim() : "N/A";
    }

    public String extractProject() {
        String[] parts = fileName.split("—|\\s|-");
        return parts.length > 4 ? parts[4].trim() : "N/A";
    }

    public String extractInventoryNumber() {
        String inventoryNumberPattern = "[0-9]{2}/[0-9]{2}-[0-9]{3}";
        Pattern pattern = Pattern.compile(inventoryNumberPattern);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return "N/A";
        }
    }

    public String extractExtension() {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1 || lastIndexOfDot == 0 || lastIndexOfDot == fileName.length() - 1) {
            return "none";
        }
        return fileName.substring(lastIndexOfDot + 1);
    }
}
