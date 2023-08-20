package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingPracticePacketOrder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingPracticePacketOrderRepository : CrudRepository<BookingPracticePacketOrder, Long> {
    fun findByBookingId(bookingId: Long): List<BookingPracticePacketOrder>
}
