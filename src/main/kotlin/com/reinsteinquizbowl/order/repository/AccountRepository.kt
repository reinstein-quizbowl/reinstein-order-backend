package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : CrudRepository<Account, Long> {
    fun findByUsername(username: String): Account?
}
