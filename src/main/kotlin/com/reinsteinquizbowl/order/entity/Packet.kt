package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class Packet(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var yearCode: String? = null,
    var number: Int? = null,
    var availableForCompetition: Boolean? = null,
    var priceAsPracticeMaterial: BigDecimal? = null,
) {
    fun isAvailableAsPracticeMaterial() = priceAsPracticeMaterial != null

    fun getName() = "$yearCode regular-season packet $number"
}
