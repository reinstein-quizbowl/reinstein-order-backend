package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.NonConferenceGame
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NonConferenceGameRepository : CrudRepository<NonConferenceGame, Long> {
    fun findByBookingId(bookingId: Long): List<NonConferenceGame>

    fun findByIdIn(ids: List<Long>): List<NonConferenceGame>
}
