package ru.gdlbo.search.searcher.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FileInfoSpecification {
    public static Specification<FileInfo> createSpecification(
            String decNumber, String deviceName, String documentType, String usedDevices,
            String project, String inventoryNumber, String lastModified, String location,
            String creationTime, String userName) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addStringPredicate(decNumber, "decNumber", root, criteriaBuilder, predicates);
            addStringPredicate(deviceName, "deviceName", root, criteriaBuilder, predicates);
            addStringPredicate(documentType, "documentType", root, criteriaBuilder, predicates);
            addStringPredicate(usedDevices, "usedDevices", root, criteriaBuilder, predicates);
            addStringPredicate(project, "project", root, criteriaBuilder, predicates);
            addStringPredicate(inventoryNumber, "inventoryNumber", root, criteriaBuilder, predicates);
            addStringPredicate(lastModified, "lastModified", root, criteriaBuilder, predicates);
            addStringPredicate(location, "location", root, criteriaBuilder, predicates);
            addStringPredicate(creationTime, "creationTime", root, criteriaBuilder, predicates);
            addUserPredicate(userName, root, criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addStringPredicate(String value, String fieldName, Root<FileInfo> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (value != null && !value.isEmpty()) {
            String pattern = "%" + value.toLowerCase() + "%";
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(fieldName)), pattern));
        }
    }

    private static void addUserPredicate(String value, Root<FileInfo> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
        if (value != null && !value.isEmpty()) {
            String pattern = "%" + value.toLowerCase() + "%";
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("username")), pattern));
        }
    }
}