package ru.gdlbo.search.searcher.repository.user

import jakarta.persistence.*
import lombok.*
import ru.gdlbo.search.searcher.repository.role.Role

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_roles")
@EqualsAndHashCode(exclude = ["user", "role"])
class UserRole(
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne
    private val user: User,

    @JoinColumn(name = "role_id", nullable = false)
    @ManyToOne
    val role: Role
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    constructor() : this(User(), Role())
}