package com.reinsteinquizbowl.order.spring

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Date

@Service
class JwtService(
    @Value("\${reinsteinquizbowl.jwtSecret}") private val secret: String,
    @Value("\${reinsteinquizbowl.jwtExpirationMs}") private val jwtExpirationMs: Long,
) {

    fun getUsernameFromToken(token: String?) =
        Jwts.parserBuilder().setSigningKey(key()).build()
            .parseClaimsJws(token).body.subject

    fun generateTokenAndExpiration(auth: Authentication): Pair<String, Instant> {
        val user = auth.getPrincipal() as UserDetails

        val now = Instant.now()
        val expiration = now + Duration.ofMillis(jwtExpirationMs)

        val token = Jwts.builder()
            .setSubject(user.username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact()

        return Pair(token, expiration)
    }

    @Suppress("ReturnCount")
    fun isTokenValid(token: String?): Boolean {
        if (token.isNullOrBlank()) return false

        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token)
            return true
        } catch (ex: MalformedJwtException) {
            System.err.println("Invalid JWT token: ${ex.message}")
        } catch (ex: ExpiredJwtException) {
            System.err.println("JWT token is expired: ${ex.message}")
        } catch (ex: UnsupportedJwtException) {
            System.err.println("JWT token is unsupported: ${ex.message}")
        } catch (ex: IllegalArgumentException) {
            System.err.println("JWT claims string is empty: ${ex.message}")
        }

        return false
    }

    private fun key() = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
}
