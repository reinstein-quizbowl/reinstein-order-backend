package com.reinsteinquizbowl.order.api

import java.math.BigDecimal

data class ApiPacket(
    var id: Long,
    var name: String,
    var yearCode: String,
    var number: Int,
    var availableForCompetition: Boolean,
    var priceAsPracticeMaterial: BigDecimal? = null,
)
