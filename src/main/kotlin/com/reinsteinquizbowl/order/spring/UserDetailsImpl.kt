package com.reinsteinquizbowl.order.spring

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class UserDetailsImpl(
    private val username: String,
    private val passwordHash: String,
) : UserDetails {
    override fun getUsername() = username
    override fun getPassword() = passwordHash

    // We don't yet have any facility for distinct roles/privilege/authorities. We can add it later if we need it.
    override fun getAuthorities() = mutableListOf<GrantedAuthority>(
        SimpleGrantedAuthority("admin"),
    )

    // We don't yet have any facility for these account lifecycle properties. We can add them later if we need them.
    override fun isEnabled() = true
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
}
