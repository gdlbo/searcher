package ru.gdlbo.search.searcher.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.gdlbo.search.searcher.repository.*
import java.util.stream.Collectors

@Service
open class CustomUserDetailsService : UserDetailsService {
    // The repository for managing users
    @Autowired
    private val userRepository: UserRepository? = null

    @Transactional
    open fun saveUser(user: ru.gdlbo.search.searcher.repository.User): ru.gdlbo.search.searcher.repository.User {
        user.password = BCryptPasswordEncoder().encode(user.password)
        return userRepository!!.save(user)
    }

    val users: List<ru.gdlbo.search.searcher.repository.User?>
        get() = userRepository!!.findAll()

    // This method is responsible for loading a user by their username
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        println("Loading user with username: $username")

        val user = userRepository!!.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }

        if (user == null) {
            throw UsernameNotFoundException("User not found")
        }

        // Create a new UserDetails object with the user's username, password, and authorities
        return User(
            user.username,
            user.password,
            mapRolesToAuthorities(user.userRoles)
        )
    }

    // This method is responsible for mapping a user's roles to their corresponding authorities
    private fun mapRolesToAuthorities(userRoles: Set<UserRole>): Collection<GrantedAuthority> {
        return userRoles.stream()
            .map { userRole: UserRole -> SimpleGrantedAuthority(userRole.role.name) }
            .collect(Collectors.toList())
    }
}