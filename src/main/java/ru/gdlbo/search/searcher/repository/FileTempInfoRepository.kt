package ru.gdlbo.search.searcher.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface FileTempInfoRepository : JpaRepository<FileTempInfo?, Long?>, JpaSpecificationExecutor<FileTempInfo?> {
    fun findByUser(user: User?): List<FileTempInfo?>?
}