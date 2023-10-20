package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.Booking
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingRepository : CrudRepository<Booking, Long> {
    fun findByCreationId(creationId: String): Booking?

    fun findByStatusCodeIn(statusCodes: List<String>): List<Booking>

    @Query(
        "select count(*) from booking where school_id = ?1 and id < ?2",
        nativeQuery = true
    )
    fun countLowerIdBookingsForSchoolId(schoolId: Long, bookingId: Long): Long

    @Query(
        "select count(*) from booking where school_id is null and id < ?1",
        nativeQuery = true
    )
    fun countLowerIdBookingsForNoSchool(bookingId: Long): Long
}
