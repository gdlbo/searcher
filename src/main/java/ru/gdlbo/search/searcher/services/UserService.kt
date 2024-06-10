package ru.gdlbo.search.searcher.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.gdlbo.search.searcher.repository.user.User
import ru.gdlbo.search.searcher.repository.user.UserRepository

@Service
class UserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    fun findByUsername(username: String?): User? {
        return if (username == null) {
            null
        } else {
            userRepository.findByUsername(username).orElse(null)
        }
    }
}
