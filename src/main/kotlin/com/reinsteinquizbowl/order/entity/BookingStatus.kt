package com.reinsteinquizbowl.order.entity

import com.reinsteinquizbowl.order.api.ApiBooking
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class BookingStatus(
    @Id var code: String? = null,
    var label: String? = null,
    var assumePacketExposure: Boolean? = null,
    var sequence: Long? = null,
) {
    fun allowsChangeByNonAdmin() = code == ApiBooking.BookingStatus.UNSUBMITTED
}
