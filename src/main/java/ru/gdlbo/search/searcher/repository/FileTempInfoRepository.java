package ru.gdlbo.search.searcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileTempInfoRepository extends JpaRepository<FileTempInfo, Long>, JpaSpecificationExecutor<FileTempInfo> {
    List<FileTempInfo> findByUser(User user);
}