package com.reinsteinquizbowl.order.spring

import com.reinsteinquizbowl.order.repository.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService : UserDetailsService {
    @Autowired private lateinit var accountRepo: AccountRepository

    override fun loadUserByUsername(username: String): UserDetails {
        val account = accountRepo.findByUsername(username) ?: throw UsernameNotFoundException("No such user as $username")

        return UserDetailsImpl(
            username = account.username!!,
            passwordHash = account.passwordHash!!,
        )
    }

    fun getCurrentUsernameAndAuthorities(): Pair<String?, List<String>> {
        val user = SecurityContextHolder.getContext().authentication

        return if (user == null || user.name.isNullOrBlank() || user.name == "anonymousUser") {
            Pair(null, emptyList())
        } else {
            Pair(user.name, user.authorities.map { it.authority })
        }
    }

    fun getCurrentUsername(): String? = getCurrentUsernameAndAuthorities().first

    fun isAuthenticated() = getCurrentUsername() != null

    fun isAdmin() = getCurrentUsernameAndAuthorities().second.contains("admin")
}
