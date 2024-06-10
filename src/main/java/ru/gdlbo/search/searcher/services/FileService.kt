package ru.gdlbo.search.searcher.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import ru.gdlbo.search.searcher.config.Config
import ru.gdlbo.search.searcher.repository.PaginatedResult
import ru.gdlbo.search.searcher.repository.files.FileInfo
import ru.gdlbo.search.searcher.repository.files.FileInfoRepository
import ru.gdlbo.search.searcher.repository.files.FileTempInfo
import ru.gdlbo.search.searcher.repository.files.FileTempInfoRepository
import ru.gdlbo.search.searcher.repository.user.User
import java.io.File
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

@Service
class FileService {
    @Autowired
    private val fileInfoRepository: FileInfoRepository? = null

    @Autowired
    private val tempInfoRepository: FileTempInfoRepository? = null

    @Autowired
    private val config: Config? = null

    @Autowired
    private val fileTempInfoRepository: FileTempInfoRepository? = null

    fun findAllTemp(): List<FileTempInfo?> {
        return tempInfoRepository!!.findAll()
    }

    fun findTempByUser(user: User?): List<FileTempInfo?>? {
        return tempInfoRepository!!.findByUser(user)
    }

    fun removeTempFile(id: Long) {
        processTempFile(id, true)
    }

    fun approveTempFile(id: Long) {
        processTempFile(id, false)
    }

    private fun processTempFile(id: Long, isRemoval: Boolean) {
        tempInfoRepository!!.findById(id).ifPresent { fileInfo: FileTempInfo ->
            val tempFile = fileInfo.location?.let { File(it) }
            tempFile?.exists()?.let { check(it) { "File " + fileInfo.location + " does not exist" } }

            if (!isRemoval) {
                val newPath = config?.path + "/" + tempFile?.name

                tempFile?.let { FileCopyUtils.copy(it, File(newPath)) }

                fileInfo.location = newPath

                fileInfoRepository!!.save(FileInfo(fileInfo))
            }

            tempFile?.delete()?.let { check(it) { "Failed to delete file " + fileInfo.location } }
            tempInfoRepository.delete(fileInfo)
        }
    }

    fun findFiles(spec: Specification<FileInfo>): List<FileInfo?> {
        return fileInfoRepository!!.findAll(spec)
    }

    fun paginateFileInfos(fileInfos: List<FileInfo?>, page: Int): PaginatedResult {
        val startIndex = page * 50
        val endIndex = min((startIndex + 50).toDouble(), fileInfos.size.toDouble()).toInt()
        val totalPages = ceil(fileInfos.size.toDouble() / 50).toInt()

        val paginatedFileInfos = fileInfos.subList(startIndex, endIndex).map { it?.toDTO() }

        return PaginatedResult(paginatedFileInfos, fileInfos.size > endIndex, page, totalPages)
    }

    fun saveOrUpdateFile(fileInfo: FileInfo) {
        fileInfoRepository!!.save(fileInfo)
    }

    fun saveTempFile(fileInfo: FileTempInfo) {
        tempInfoRepository!!.save(fileInfo)
    }

    fun getFileById(id: Long): Optional<FileInfo?> {
        return fileInfoRepository!!.findById(id)
    }

    fun getTempFileById(id: Long): Optional<FileTempInfo?> {
        return fileTempInfoRepository!!.findById(id)
    }

    val isAnyFilePresent: Boolean
        get() = fileInfoRepository!!.count() > 0

    fun doesFileExist(id: Long): Boolean {
        return fileInfoRepository!!.existsById(id)
    }

    fun existsByDecNumber(decNumber: String?): Boolean {
        return fileInfoRepository!!.existsByDecNumber(decNumber.toString())
    }
}