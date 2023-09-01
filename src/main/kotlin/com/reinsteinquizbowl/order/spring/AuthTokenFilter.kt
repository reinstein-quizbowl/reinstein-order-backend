package com.reinsteinquizbowl.order.spring

import com.reinsteinquizbowl.order.controller.BookingController
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class AuthTokenFilter : OncePerRequestFilter() {
    @Autowired private lateinit var jwt: JwtService
    @Autowired private lateinit var userService: UserService

    private val logger = LoggerFactory.getLogger(BookingController::class.java)

    @Suppress("TooGenericExceptionCaught")
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val token = parseJwt(request)
            if (token != null && jwt.isTokenValid(token)) {
                val username = jwt.getUsernameFromToken(token)

                val details = userService.loadUserByUsername(username)
                val auth = UsernamePasswordAuthenticationToken(details, null, details.authorities)
                auth.details = WebAuthenticationDetailsSource().buildDetails(request)

                SecurityContextHolder.getContext().authentication = auth
            }
        } catch (ex: Exception) {
            logger.warn("Cannot set user authentication: ${ex.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun parseJwt(request: HttpServletRequest): String? {
        val header: String? = request.getHeader("Authorization")

        return header
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { it.startsWith(HEADER_PREFIX) }
            ?.removePrefix(HEADER_PREFIX)
    }

    companion object {
        private const val HEADER_PREFIX = "Bearer "
    }
}
