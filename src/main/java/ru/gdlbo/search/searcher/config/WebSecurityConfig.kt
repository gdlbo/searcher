package ru.gdlbo.search.searcher.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import ru.gdlbo.search.searcher.repository.user.UserRepository
import java.util.stream.Collectors

@Configuration
@EnableWebSecurity
open class WebSecurityConfig {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Bean
    open fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username: String ->
            userRepository.findByUsername(username)
                .map { user ->
                    User(
                        user.username,
                        user.password,
                        user.userRoles.stream()
                            .map { userRole -> SimpleGrantedAuthority(userRole.role.name) }
                            .collect(Collectors.toList())
                    )
                }
                .orElseThrow()
        }
    }

    @Bean
    @Throws(Exception::class)
    open fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder())
        return authenticationManagerBuilder.build()
    }

    @Bean
    @Order(1)
    @Throws(Exception::class)
    open fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/api/history").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/download").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/changeCredentials").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/upload").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/update").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/searchTempFile").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/removeTempFile").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/web/replaceTempFile").hasAnyRole("ADMIN", "USER")
                    .requestMatchers("/api/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .csrf { csrfConfigurer -> csrfConfigurer.disable() }
        return http.build()
    }

    @Bean
    @Order(2)
    @Throws(Exception::class)
    open fun formLoginFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/**")
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/css/**").permitAll()
                    .requestMatchers("/js/**").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/register").permitAll()
                    .requestMatchers("/login").permitAll()
                    .requestMatchers("/review").authenticated()
                    .anyRequest().authenticated()
            }
            .formLogin { formLogin ->
                formLogin
                    .loginPage("/auth/login")
                    .failureUrl("/auth/login?error=true")
                    .defaultSuccessUrl("/search", true)
                    .permitAll()
            }
            .csrf { csrfConfigurer -> csrfConfigurer.disable() }
        return http.build()
    }

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}