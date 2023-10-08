package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.repository.BookingPracticeCompilationOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticePacketOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeStateSeriesOrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PracticeMaterialService {
    @Autowired private lateinit var compilationOrderRepo: BookingPracticeCompilationOrderRepository
    @Autowired private lateinit var packetOrderRepo: BookingPracticePacketOrderRepository
    @Autowired private lateinit var stateSeriesOrderRepo: BookingPracticeStateSeriesOrderRepository

    fun deleteOrders(booking: Booking) {
        val compilationOrders = compilationOrderRepo.findByBookingId(booking.id!!)
        compilationOrderRepo.deleteAll(compilationOrders)

        val packetOrders = packetOrderRepo.findByBookingId(booking.id!!)
        packetOrderRepo.deleteAll(packetOrders)

        val stateSeriesOrders = stateSeriesOrderRepo.findByBookingId(booking.id!!)
        stateSeriesOrderRepo.deleteAll(stateSeriesOrders)
    }
}
