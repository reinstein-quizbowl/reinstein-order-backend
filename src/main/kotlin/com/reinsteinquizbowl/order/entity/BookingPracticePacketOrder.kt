package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
data class BookingPracticePacketOrder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var bookingId: Long? = null,
    @ManyToOne var packet: Packet? = null,
) {
    companion object {
        val YEAR_AND_NUMBER_COMPARATOR = compareBy<BookingPracticePacketOrder>({ it.packet!!.yearCode }, { it.packet!!.number })
    }
}
