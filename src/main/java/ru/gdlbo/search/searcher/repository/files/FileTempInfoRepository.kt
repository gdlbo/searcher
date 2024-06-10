package ru.gdlbo.search.searcher.repository.files

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import ru.gdlbo.search.searcher.repository.user.User

@Repository
interface FileTempInfoRepository : JpaRepository<FileTempInfo?, Long?>, JpaSpecificationExecutor<FileTempInfo?> {
    fun findByUser(user: User?): List<FileTempInfo?>?
}