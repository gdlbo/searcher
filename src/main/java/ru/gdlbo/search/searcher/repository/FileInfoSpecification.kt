package ru.gdlbo.search.searcher.repository

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification
import java.util.*

object FileInfoSpecification {
    fun createSpecification(
        decNumber: String?, deviceName: String?, documentType: String?, usedDevices: String?,
        project: String?, inventoryNumber: String?, lastModified: String?, location: String?,
        creationTime: String?, userName: String?
    ): Specification<FileInfo> {
        return Specification { root: Root<FileInfo>, _: CriteriaQuery<*>?, criteriaBuilder: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList()
            addStringPredicate(decNumber, "decNumber", root, criteriaBuilder, predicates)
            addStringPredicate(deviceName, "deviceName", root, criteriaBuilder, predicates)
            addStringPredicate(documentType, "documentType", root, criteriaBuilder, predicates)
            addStringPredicate(usedDevices, "usedDevices", root, criteriaBuilder, predicates)
            addStringPredicate(project, "project", root, criteriaBuilder, predicates)
            addStringPredicate(inventoryNumber, "inventoryNumber", root, criteriaBuilder, predicates)
            addStringPredicate(lastModified, "lastModified", root, criteriaBuilder, predicates)
            addStringPredicate(location, "location", root, criteriaBuilder, predicates)
            addStringPredicate(creationTime, "creationTime", root, criteriaBuilder, predicates)
            addUserPredicate(userName, root, criteriaBuilder, predicates)
            criteriaBuilder.and(*predicates.toTypedArray<Predicate>())
        }
    }

    private fun addStringPredicate(
        value: String?,
        fieldName: String,
        root: Root<FileInfo>,
        criteriaBuilder: CriteriaBuilder,
        predicates: MutableList<Predicate>
    ) {
        if (!value.isNullOrEmpty()) {
            val pattern = "%" + value.lowercase(Locale.getDefault()) + "%"
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(fieldName)), pattern))
        }
    }

    private fun addUserPredicate(
        value: String?,
        root: Root<FileInfo>,
        criteriaBuilder: CriteriaBuilder,
        predicates: MutableList<Predicate>
    ) {
        if (!value.isNullOrEmpty()) {
            val pattern = "%" + value.lowercase(Locale.getDefault()) + "%"
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get<Any>("user").get("username")), pattern))
        }
    }
}