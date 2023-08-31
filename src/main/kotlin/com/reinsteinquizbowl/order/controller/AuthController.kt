package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiLoginRequest
import com.reinsteinquizbowl.order.api.ApiLoginResponse
import com.reinsteinquizbowl.order.spring.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["*"], maxAge = 3600)
class AuthController {
    @Autowired private lateinit var authManager: AuthenticationManager
    @Autowired private lateinit var jwt: JwtService

    @PostMapping("/auth/login")
    fun authenticateUser(@RequestBody loginRequest: ApiLoginRequest): ApiLoginResponse {
        val auth: Authentication = authManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )

        SecurityContextHolder.getContext().authentication = auth

        val (token, tokenExpires) = jwt.generateTokenAndExpiration(auth)
        val userDetails = auth.principal as UserDetails
        val roles: List<String> = userDetails.authorities
            .map { it.authority }
            .toList()

        return ApiLoginResponse(
            username = userDetails.username,
            roles = roles,
            token = token,
            tokenExpires = tokenExpires,
        )
    }
}
