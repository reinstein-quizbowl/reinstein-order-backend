package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiPacketExposure
import com.reinsteinquizbowl.order.entity.Packet
import com.reinsteinquizbowl.order.repository.PacketExposureRepository
import com.reinsteinquizbowl.order.repository.PacketRepository
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.service.PacketAssignmentService
import com.reinsteinquizbowl.order.service.YearService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class PacketExposureController {
    @Autowired private lateinit var repo: PacketExposureRepository
    @Autowired private lateinit var packetRepo: PacketRepository
    @Autowired private lateinit var packetAssignmentService: PacketAssignmentService
    @Autowired private lateinit var yearService: YearService
    @Autowired private lateinit var convert: Converter

    @GetMapping("/packetExposures")
    fun getExposures(
        @RequestParam yearCode: String? = null,
    ): List<ApiPacketExposure> {
        val year = yearService.getYear(yearCode) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Year is required")

        val packetIds = packetRepo.findByYearCode(year.code!!)
            .mapNotNull(Packet::id)

        val exposures = repo.findByPacketIdIn(packetIds)
        return exposures.map(convert::toApi)
    }

    @GetMapping("/packetExposures/doubleBookings")
    @PreAuthorize("hasAuthority('admin')")
    fun getDoubleBookings(): List<ApiPacketExposure> {
        val doubleBookings = packetAssignmentService.findDoubleBookings()

        return doubleBookings.flatMap { repo.findByExposedSchoolIdAndPacketId(it.schoolId, it.packetId) }
            .map(convert::toApi)
    }
}
