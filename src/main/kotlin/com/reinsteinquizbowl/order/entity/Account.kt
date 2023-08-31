package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Account(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var username: String? = null,
    var passwordHash: String? = null,
)
