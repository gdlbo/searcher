package ru.gdlbo.search.searcher.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.lang.NonNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.stream.Collectors

@Component
class LoggingFilter : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        @NonNull request: HttpServletRequest,
        @NonNull response: HttpServletResponse,
        @NonNull filterChain: FilterChain
    ) {
        // Get the username and roles of the user making the request
        var username: String? = null
        var roles: String? = null
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal is UserDetails) {
            username = principal.username
            val authorities = principal.authorities
            roles = authorities.stream()
                .map { obj: GrantedAuthority -> obj.authority }
                .collect(Collectors.joining(", "))
        }

        println("Request received: " + request.method + " " + request.requestURI + " from " + request.remoteAddr + " by " + username + " with roles " + roles)

        // Continue with the request processing
        filterChain.doFilter(request, response)
    }
}