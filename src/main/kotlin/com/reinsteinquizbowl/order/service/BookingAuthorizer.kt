package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.spring.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BookingAuthorizer {
    @Autowired private lateinit var user: UserService

    fun authorize(booking: Booking, creationId: String) {
        if (creationId != booking.creationId) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }

        if (!user.isAdmin() && booking.status?.allowsChangeByNonAdmin() == false) { // new bookings come in with null status, which can be changed by non-admin
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        }
    }
}
