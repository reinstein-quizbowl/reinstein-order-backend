package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiPacket
import com.reinsteinquizbowl.order.api.ApiPacketAssignment
import com.reinsteinquizbowl.order.entity.Packet
import com.reinsteinquizbowl.order.entity.Year
import com.reinsteinquizbowl.order.repository.PacketRepository
import com.reinsteinquizbowl.order.repository.YearRepository
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.service.PacketAssignmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class PacketController {
    @Autowired private lateinit var repo: PacketRepository
    @Autowired private lateinit var yearRepo: YearRepository
    @Autowired private lateinit var assignmentService: PacketAssignmentService
    @Autowired private lateinit var bookingService: BookingService
    @Autowired private lateinit var convert: Converter

    @GetMapping("/packets")
    fun getPackets(
        @RequestParam yearCode: String? = null,
        @RequestParam filter: String? = null,
    ): List<ApiPacket> {
        val year = if (yearCode.isNullOrBlank()) null else getYear(yearCode)

        var packets = if (year == null) repo.findAll() else repo.findByYearCode(year.code!!)
        when (filter) {
            "availableForPractice" -> packets = packets.filter { it.isAvailableAsPracticeMaterial() }
            "availableForCompetition" -> packets = packets.filter { it.availableForCompetition == true }
        }

        return packets
            .sortedWith(compareBy<Packet> { it.yearCode }.thenBy { it.number }) // presuming the year codes make sense for that
            .map(convert::toApi)
    }

    @GetMapping("/bookings/{creationId}/potentialPacketAssignments")
    fun getPotentialPacketAssignments(@PathVariable(name = "creationId") bookingCreationId: String): List<ApiPacketAssignment> {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        return assignmentService.findAssignments(booking).sortedWith(PacketAssignmentService.ASSIGNMENT_DISPLAY_COMPARATOR)
    }

    @PostMapping("/bookings/{creationId}/packetAssignments")
    fun makePacketAssignments(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @RequestBody input: List<ApiPacketAssignment>,
    ): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        if (input.any(ApiPacketAssignment::isMissingPacketAssignment)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Incomplete assignments cannot be used")
        }

        // We recalculate potential assignments to (a) make sure nothing has changed since the input was previously submitted and (b) prevent people from messing with assignments and substituting their own. This requires that findAssignments() be completely deterministic.
        val recalculated = assignmentService.findAssignments(booking).toSet()
        if (input.toSet() != recalculated) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A problem occurred assigning packets. Possibly another booking went through while you were working on yours.")
        }

        assignmentService.assign(booking, input)

        val refetched = bookingService.refetch(booking)
        return convert.toApi(refetched)
    }

    @DeleteMapping("/bookings/{creationId}/packetAssignments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePacketAssignments(@PathVariable(name = "creationId") bookingCreationId: String): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        assignmentService.unassign(booking)

        val refetched = bookingService.refetch(booking)
        return convert.toApi(refetched)
    }

    private fun getYear(yearCode: String?): Year {
        yearCode ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Missing year")
        return yearRepo.findByIdOrNull(yearCode) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid year")
    }
}
