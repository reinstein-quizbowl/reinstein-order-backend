package com.reinsteinquizbowl.order.api

import java.math.BigDecimal
import java.time.LocalDate

data class ApiYear(
    var code: String,
    var name: String,
    var startDate: LocalDate,
    var endDate: LocalDate,
    var questionsShipStarting: LocalDate,
    var maximumPacketPracticeMaterialPrice: BigDecimal? = null,
)
