package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.entity.BookingConferencePacket
import com.reinsteinquizbowl.order.entity.BookingConferenceSchool
import com.reinsteinquizbowl.order.repository.BookingConferencePacketRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.PacketRepository
import com.reinsteinquizbowl.order.repository.SchoolRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BookingConferenceService {
    @Autowired private lateinit var bookingConferencePacketRepo: BookingConferencePacketRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var packetRepo: PacketRepository
    @Autowired private lateinit var schoolRepo: SchoolRepository

    fun adjustSchools(conference: BookingConference, inputSchoolIds: List<Long>) {
        val extant = conference.id?.let { bookingConferenceSchoolRepo.findByBookingConferenceId(it) } ?: emptyList()

        val (toKeep, toDelete) = extant.partition { inputSchoolIds.contains(it.school?.id) }

        for (schoolId in inputSchoolIds) {
            if (toKeep.none { it.school?.id == schoolId }) {
                val school = schoolRepo.findByIdOrNull(schoolId)
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school ID: $schoolId")

                bookingConferenceSchoolRepo.save(
                    BookingConferenceSchool(
                        bookingConference = conference,
                        school = school,
                    )
                )
            }
        }

        bookingConferenceSchoolRepo.deleteAll(toDelete)
    }

    fun adjustPackets(conference: BookingConference, inputPacketIds: List<Long>) {
        val extant = conference.id?.let { bookingConferencePacketRepo.findByBookingConferenceId(it) } ?: emptyList()

        val (toKeep, toDelete) = extant.partition { inputPacketIds.contains(it.assignedPacket?.id) }

        for (inputPacketId in inputPacketIds) {
            if (toKeep.none { it.assignedPacket?.id == inputPacketId }) {
                val packet = packetRepo.findByIdOrNull(inputPacketId)
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid packet ID: $inputPacketId")

                bookingConferencePacketRepo.save(
                    BookingConferencePacket(
                        bookingConferenceId = conference.id,
                        assignedPacket = packet
                    )
                )
            }
        }

        bookingConferencePacketRepo.deleteAll(toDelete)
    }
}
