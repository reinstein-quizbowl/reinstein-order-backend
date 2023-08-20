package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingPracticeCompilationOrder
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingPracticeCompilationOrderRepository : CrudRepository<BookingPracticeCompilationOrder, Long> {
    fun findByBookingId(bookingId: Long): List<BookingPracticeCompilationOrder>
}
