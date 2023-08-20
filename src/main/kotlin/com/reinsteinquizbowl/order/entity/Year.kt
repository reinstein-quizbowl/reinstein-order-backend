package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate

@Entity
data class Year(
    @Id var code: String? = null,
    var name: String? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var questionsShipStarting: LocalDate? = null,
    var maximumPacketPracticeMaterialPrice: BigDecimal? = null,
)
