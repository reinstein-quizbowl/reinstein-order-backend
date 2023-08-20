package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingConferenceSchool
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingConferenceSchoolRepository : CrudRepository<BookingConferenceSchool, Long> {
    fun findByBookingConferenceId(bookingConferenceId: Long): List<BookingConferenceSchool>
    fun countByBookingConferenceId(bookingConferenceId: Long): Long

    @Query(
        "SELECT school_id FROM booking_conference_school WHERE booking_conference_id = ?1",
        nativeQuery = true
    )
    fun findSchoolIdsByBookingConferenceId(bookingConferenceId: Long): List<Long>
}
