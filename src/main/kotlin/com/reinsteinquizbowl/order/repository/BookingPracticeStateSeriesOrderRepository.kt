package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingPracticeStateSeriesOrder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingPracticeStateSeriesOrderRepository : CrudRepository<BookingPracticeStateSeriesOrder, Long> {
    fun findByBookingId(bookingId: Long): List<BookingPracticeStateSeriesOrder>
}
