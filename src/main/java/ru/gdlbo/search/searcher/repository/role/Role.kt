package ru.gdlbo.search.searcher.repository.role

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "roles")
@EqualsAndHashCode
class Role(val name: String) {
    constructor() : this("USER")

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
}