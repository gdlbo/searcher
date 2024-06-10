package ru.gdlbo.search.searcher.repository.files

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface FileInfoRepository : JpaRepository<FileInfo, Long>, JpaSpecificationExecutor<FileInfo> {
    fun existsByDecNumber(decNumber: String): Boolean
}