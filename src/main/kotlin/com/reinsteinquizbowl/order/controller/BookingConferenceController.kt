package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiBookingConference
import com.reinsteinquizbowl.order.api.ApiPacket
import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.repository.BookingConferencePacketRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.service.BookingConferenceService
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.spring.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class BookingConferenceController {
    @Autowired private lateinit var repo: BookingConferenceRepository
    @Autowired private lateinit var bookingConferencePacketRepo: BookingConferencePacketRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var service: BookingConferenceService
    @Autowired private lateinit var bookingService: BookingService
    @Autowired private lateinit var convert: Converter
    @Autowired private lateinit var user: UserService

    @PostMapping("/bookings/{bookingCreationId}/conference")
    fun upsert(
        @PathVariable bookingCreationId: String,
        @RequestBody input: ApiBookingConference,
    ): ApiBooking {
        val booking = bookingService.findOrCreateThenAuthorize(bookingCreationId)

        val entity = repo.findByBookingId(booking.id!!) ?: BookingConference(booking = booking)

        entity.name = input.name ?: entity.name
        entity.name ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required")

        entity.packetsRequested = input.packetsRequested ?: entity.packetsRequested
        entity.packetsRequested ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "PacketsRequested is required")
        if (entity.packetsRequested!! <= 0) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "PacketsRequested must be positive")

        val savedEntity = repo.save(entity) // needs to be before we create BookingConferenceSchools because those ultimately need the ID

        input.schoolIds?.let { inputSchoolIds -> service.adjustSchools(savedEntity, inputSchoolIds) }

        if (user.isAdmin()) {
            input.assignedPackets?.let { inputPackets ->
                val inputPacketIds = inputPackets.map(ApiPacket::id)
                service.adjustPackets(savedEntity, inputPacketIds)
            }
        }

        return convert.toApi(bookingService.refetch(booking))
    }

    @DeleteMapping("/bookings/{bookingCreationId}/conference")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable bookingCreationId: String): ApiBooking {
        val booking = bookingService.findOrCreateThenAuthorize(bookingCreationId)

        val conference = repo.findByBookingId(booking.id!!)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No conference for this booking")

        val packets = bookingConferencePacketRepo.findByBookingConferenceId(conference.id!!)
        bookingConferencePacketRepo.deleteAll(packets)

        val schools = bookingConferenceSchoolRepo.findByBookingConferenceId(conference.id!!)
        bookingConferenceSchoolRepo.deleteAll(schools)

        repo.delete(conference)

        return convert.toApi(bookingService.refetch(booking))
    }
}
