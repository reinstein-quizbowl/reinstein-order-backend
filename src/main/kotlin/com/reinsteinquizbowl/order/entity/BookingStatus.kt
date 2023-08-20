package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class BookingStatus(
    @Id var code: String? = null,
    var label: String? = null,
    var assumePacketExposure: Boolean? = null,
    var sequence: Long? = null,
)
