package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class InvoiceLine(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var bookingId: Long? = null,
    var itemType: String? = null,
    var itemId: String? = null,
    var label: String? = null,
    var quantity: Int? = null,
    var unitCost: BigDecimal? = null,
    var sequence: Long? = null,
) {
    fun getTotalCost() = (unitCost ?: BigDecimal.ZERO) * (quantity ?: 1).toBigDecimal()
}
