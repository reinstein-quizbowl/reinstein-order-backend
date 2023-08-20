package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingConferencePacket
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingConferencePacketRepository : CrudRepository<BookingConferencePacket, Long> {
    fun findByBookingConferenceId(bookingConferenceId: Long): List<BookingConferencePacket>
    fun findByBookingConferenceIdIn(bookingConferenceIds: List<Long>): List<BookingConferencePacket>
}
