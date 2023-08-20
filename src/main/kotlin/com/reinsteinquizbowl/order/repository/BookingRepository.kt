package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.Booking
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingRepository : CrudRepository<Booking, Long> {
    fun findByCreationId(creationId: String): Booking?

    fun findBySchoolId(schoolId: Long): List<Booking>

    fun findBySchoolIdIn(schoolIds: List<Long>): List<Booking>

    fun findByStatusCodeIn(statusCodes: List<String>): List<Booking>
}
