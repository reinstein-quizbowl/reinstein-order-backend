package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.Booking
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BookingAuthorizer {
    fun authorize(booking: Booking, creationId: String) {
        if (creationId != booking.creationId) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }
}
