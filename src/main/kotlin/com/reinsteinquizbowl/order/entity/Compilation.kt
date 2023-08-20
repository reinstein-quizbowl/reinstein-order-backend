package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class Compilation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var name: String? = null,
    var description: String? = null,
    var price: BigDecimal? = null,
    var available: Boolean? = null,
    var sequence: Long? = null,
)
