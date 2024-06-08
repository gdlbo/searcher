package ru.gdlbo.search.searcher.repository

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Data
import lombok.EqualsAndHashCode
import lombok.NoArgsConstructor

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@EqualsAndHashCode(exclude = ["userRoles"])
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    var username: String? = null

    var password: String? = null

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var userRoles: MutableSet<UserRole> = HashSet()
}