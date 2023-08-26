package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.entity.BookingPracticeCompilationOrder
import com.reinsteinquizbowl.order.entity.BookingPracticePacketOrder
import com.reinsteinquizbowl.order.entity.BookingPracticeStateSeriesOrder
import com.reinsteinquizbowl.order.repository.BookingPracticeCompilationOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticePacketOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeStateSeriesOrderRepository
import com.reinsteinquizbowl.order.repository.CompilationRepository
import com.reinsteinquizbowl.order.repository.PacketRepository
import com.reinsteinquizbowl.order.repository.StateSeriesRepository
import com.reinsteinquizbowl.order.service.BookingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class BookingPracticeQuestionsController {
    @Autowired private lateinit var bookingPracticeStateSeriesRepo: BookingPracticeStateSeriesOrderRepository
    @Autowired private lateinit var bookingPracticePacketRepo: BookingPracticePacketOrderRepository
    @Autowired private lateinit var bookingPracticeCompilationRepo: BookingPracticeCompilationOrderRepository
    @Autowired private lateinit var stateSeriesRepo: StateSeriesRepository
    @Autowired private lateinit var compilationRepo: CompilationRepository
    @Autowired private lateinit var packetRepo: PacketRepository
    @Autowired private lateinit var bookingService: BookingService

    // This is an upsert
    @PostMapping("/bookings/{creationId}/stateSeries")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun setStateSeries(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @RequestBody stateSeriesIds: List<Long>
    ): String {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val extant = bookingPracticeStateSeriesRepo.findByBookingId(booking.id!!)

        val (toKeep, toDelete) = extant.partition { stateSeriesIds.contains(it.stateSeries?.id) }

        for (stateSeriesId in stateSeriesIds) {
            if (toKeep.none { it.stateSeries?.id == stateSeriesId }) {
                val stateSeries = stateSeriesRepo.findByIdOrNull(stateSeriesId)
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid State Series ID: $stateSeriesId")

                bookingPracticeStateSeriesRepo.save(
                    BookingPracticeStateSeriesOrder(
                        bookingId = booking.id,
                        stateSeries = stateSeries,
                    )
                )
            }
        }

        bookingPracticeStateSeriesRepo.deleteAll(toDelete)

        return "{}"
    }

    // This is an upsert
    @PostMapping("/bookings/{creationId}/practicePackets")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun setPackets(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @RequestBody packetIds: List<Long>
    ): String {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val extant = bookingPracticePacketRepo.findByBookingId(booking.id!!)

        val (toKeep, toDelete) = extant.partition { packetIds.contains(it.packet?.id) }

        for (packetId in packetIds) {
            if (toKeep.none { it.packet?.id == packetId }) {
                val packet = packetRepo.findByIdOrNull(packetId)
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid packet ID: $packetId")

                bookingPracticePacketRepo.save(
                    BookingPracticePacketOrder(
                        bookingId = booking.id,
                        packet = packet,
                    )
                )
            }
        }

        bookingPracticePacketRepo.deleteAll(toDelete)

        return "{}"
    }

    // This is an upsert
    @PostMapping("/bookings/{creationId}/practiceCompilations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun setCompilations(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @RequestBody compilationIds: List<Long>
    ): String {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val extant = bookingPracticeCompilationRepo.findByBookingId(booking.id!!)

        val (toKeep, toDelete) = extant.partition { compilationIds.contains(it.compilation?.id) }

        for (compilationId in compilationIds) {
            if (toKeep.none { it.compilation?.id == compilationId }) {
                val compilation = compilationRepo.findByIdOrNull(compilationId)
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid compilation ID: $compilationId")

                bookingPracticeCompilationRepo.save(
                    BookingPracticeCompilationOrder(
                        bookingId = booking.id,
                        compilation = compilation,
                    )
                )
            }
        }

        bookingPracticeCompilationRepo.deleteAll(toDelete)

        return "{}"
    }
}
