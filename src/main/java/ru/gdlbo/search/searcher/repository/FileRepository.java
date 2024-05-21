package ru.gdlbo.search.searcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileInfo, Long>, JpaSpecificationExecutor<FileInfo> {
    List<FileInfo> findByDecNumber(String decNumber);

    List<FileInfo> findByDeviceName(String deviceName);

    List<FileInfo> findByDocumentType(String documentType);

    List<FileInfo> findByUsedDevices(String usedDevices);

    List<FileInfo> findByProject(String project);

    List<FileInfo> findByInventoryNumber(String inventoryNumber);

    List<FileInfo> findByLastModified(String lastModified);

    List<FileInfo> findByLocation(String location);

    List<FileInfo> findByCreationTime(String creationTime);

    List<FileInfo> findByUser(User user);

    boolean existsByDecNumber(String decNumber);
}