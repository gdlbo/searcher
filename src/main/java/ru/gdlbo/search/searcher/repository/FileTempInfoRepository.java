package ru.gdlbo.search.searcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileTempInfoRepository extends JpaRepository<FileTempInfo, Long>, JpaSpecificationExecutor<FileTempInfo> {

}