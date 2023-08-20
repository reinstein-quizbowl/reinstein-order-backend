package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingConference
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingConferenceRepository : CrudRepository<BookingConference, Long> {
    // The data model allows multiple results for this, but we currently don't support that at the application level
    fun findByBookingId(bookingId: Long): BookingConference?
}
