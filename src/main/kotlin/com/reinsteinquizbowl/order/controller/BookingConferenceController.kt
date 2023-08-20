package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiBookingConference
import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.entity.BookingConferenceSchool
import com.reinsteinquizbowl.order.repository.BookingConferenceRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.SchoolRepository
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class BookingConferenceController {
    @Autowired private lateinit var repo: BookingConferenceRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var schoolRepo: SchoolRepository
    @Autowired private lateinit var bookingService: BookingService
    @Autowired private lateinit var convert: Converter

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

        input.schoolIds?.let { schoolIds ->
            val extant = entity.id?.let { bookingConferenceSchoolRepo.findByBookingConferenceId(it) } ?: emptyList()

            val (toKeep, toDelete) = extant.partition { schoolIds.contains(it.school?.id) }

            for (schoolId in schoolIds) {
                if (toKeep.none { it.school?.id == schoolId }) {
                    val school = schoolRepo.findByIdOrNull(schoolId)
                        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school ID: $schoolId")

                    bookingConferenceSchoolRepo.save(
                        BookingConferenceSchool(
                            bookingConference = savedEntity,
                            school = school,
                        )
                    )
                }
            }

            bookingConferenceSchoolRepo.deleteAll(toDelete)
        }

        // assignedPackets is not being managed here

        return convert.toApi(bookingService.refetch(booking))
    }
}
