package ru.gdlbo.search.searcher.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gdlbo.search.searcher.repository.Role;
import ru.gdlbo.search.searcher.repository.User;
import ru.gdlbo.search.searcher.repository.UserRepository;
import ru.gdlbo.search.searcher.repository.UserRole;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    // The repository for managing users
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User saveUser(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // This method is responsible for loading a user by their username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user with username: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Create a new UserDetails object with the user's username, password, and authorities
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getUserRoles())
        );
    }

    // This method is responsible for mapping a user's roles to their corresponding authorities
    public Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<UserRole> userRoles) {
        return userRoles.stream()
                .map(UserRole::getRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}