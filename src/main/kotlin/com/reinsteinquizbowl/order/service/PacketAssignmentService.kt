package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.api.ApiPacketAssignment
import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.entity.BookingConferencePacket
import com.reinsteinquizbowl.order.entity.NonConferenceGame
import com.reinsteinquizbowl.order.entity.Packet
import com.reinsteinquizbowl.order.entity.School
import com.reinsteinquizbowl.order.repository.BookingConferencePacketRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import com.reinsteinquizbowl.order.repository.PacketRepository
import com.reinsteinquizbowl.order.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Service
class PacketAssignmentService {
    @Autowired private lateinit var bookingConferenceRepo: BookingConferenceRepository
    @Autowired private lateinit var bookingConferencePacketRepo: BookingConferencePacketRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var nonConferenceGameRepo: NonConferenceGameRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository
    @Autowired private lateinit var packetRepo: PacketRepository

    // This must be deterministic! Assignments are generated multiple times and compared; if identical input data can produce different results, the process will fail.
    fun findAssignments(booking: Booking): List<ApiPacketAssignment> {
        val needs = mutableListOf<ApiPacketAssignment>()

        bookingConferenceRepo.findByBookingId(booking.id!!)?.let { conference ->
            val schoolIds = bookingConferenceSchoolRepo.findSchoolIdsByBookingConferenceId(conference.id!!)
            for (i in 1..(conference.packetsRequested ?: 0)) {
                needs.add(
                    ApiPacketAssignment(
                        type = ApiPacketAssignment.Type.CONFERENCE,
                        id = conference.id!!,
                        date = LocalDate.MIN,
                        sequence = i,
                        description = "conference packet #$i",
                        schoolIds = schoolIds,
                    )
                )
            }
        }

        val nonConferenceGames = nonConferenceGameRepo.findByBookingId(booking.id!!)
        if (nonConferenceGames.isNotEmpty()) {
            val gameIds = nonConferenceGames.mapNotNull(NonConferenceGame::id)
            val gameSchools = nonConferenceGameSchoolRepo.findByNonConferenceGameIdIn(gameIds)

            val schoolsById: Map<Long, School> = gameSchools.associateBy({ it.school!!.id!! }, { it.school!! })
            val schoolIdsByGameId: Map<Long, List<Long>> = gameSchools.groupBy(keySelector = { it.nonConferenceGame!!.id!! }, valueTransform = { it.school!!.id!! })

            for (i in nonConferenceGames.indices) {
                val game = nonConferenceGames[i]
                val schoolIds = schoolIdsByGameId[game.id!!]!!
                val schoolNames: List<String> = schoolIds.mapNotNull { schoolsById[it] }.mapNotNull(School::shortName)

                needs.add(
                    ApiPacketAssignment(
                        type = ApiPacketAssignment.Type.NON_CONFERENCE_GAME,
                        id = game.id!!,
                        date = game.date!!,
                        sequence = i,
                        description = "the non-conference game on ${game.date!!.format(DATE_FORMATTER)} (${Util.makeEnglishList(schoolNames)})",
                        schoolIds = schoolIds,
                    )
                )
            }
        }

        return findAssignments(needs)
    }

    // This must be deterministic! Assignments are generated multiple times and compared; if identical input data can produce different results, the process will fail.
    private fun findAssignments(needs: List<ApiPacketAssignment>): List<ApiPacketAssignment> {
        val allSchoolIds: List<Long> = needs.map(ApiPacketAssignment::schoolIds).flatten()

        val availablePacketIdsBySchoolId: MutableMap<Long, MutableList<Long>> = mutableMapOf() // This is updated live. It starts out based on persisted data.
        for (schoolId in allSchoolIds) {
            availablePacketIdsBySchoolId[schoolId] = packetRepo.findIdsOfUnexposedPacketsAvailableForCompetition(listOf(schoolId)).toMutableList()
        }

        val allPacketIds: List<Long> = availablePacketIdsBySchoolId.values.flatten()
        val allPackets = packetRepo.findByIdIn(allPacketIds)
        val packetsById: Map<Long, Packet> = allPackets.associateBy { it.id!! }

        val gameAssignmentPriorityOrder: Comparator<ApiPacketAssignment> =
            compareBy<ApiPacketAssignment> { findPacketIdsAvailableFor(it.schoolIds, availablePacketIdsBySchoolId).size } // if fewer packets available, assign sooner
                .thenBy { it.date } // then earlier games first (conference games have already been marked as early as possible)
                .thenBy { it.sequence } // to try to keep conference games in order
                .thenBy { it.hashCode() } // to make the sort, and thus the whole operation, deterministic

        val unseen = needs.toMutableList()
        val seen = mutableListOf<ApiPacketAssignment>()

        while (unseen.isNotEmpty()) {
            val toAssign = unseen.minWithOrNull(gameAssignmentPriorityOrder) ?: break // the fallback shouldn't happen

            val packetsAvailable = findPacketIdsAvailableFor(toAssign.schoolIds, availablePacketIdsBySchoolId) // This is being called n log n times via the comparator above, then again here. Until proven otherwise, I assume it's efficient enough not to matter.
                .mapNotNull { packetsById[it] }
            if (packetsAvailable.isNotEmpty()) {
                val packet = packetsAvailable.minWith(PACKET_ASSIGNMENT_PRIORITY_COMPARATOR)

                // assign the packet
                toAssign.packetId = packet.id!!

                // and mark it as not available to be assigned for any of those schools
                for (schoolId in toAssign.schoolIds) {
                    availablePacketIdsBySchoolId[schoolId]?.remove(packet.id!!)
                }
            }

            unseen.remove(toAssign)
            seen.add(toAssign)
        }

        return seen
    }

    private fun findPacketIdsAvailableFor(schoolIds: List<Long>, availablePacketIdsBySchoolId: Map<Long, List<Long>>): Set<Long> {
        var intersection: Set<Long> = availablePacketIdsBySchoolId.values.flatten().toSet()
        for (schoolId in schoolIds) {
            val availableForThisSchool = availablePacketIdsBySchoolId[schoolId] ?: emptyList()
            intersection = intersection.intersect(availableForThisSchool.toSet())
        }

        return intersection
    }

    fun assign(booking: Booking, assignments: List<ApiPacketAssignment>) {
        require(assignments.none(ApiPacketAssignment::isMissingPacketAssignment)) { "Incomplete assignments" }

        val conference = bookingConferenceRepo.findByBookingId(booking.id!!)
        require(conference != null || assignments.none { it.type == ApiPacketAssignment.Type.CONFERENCE }) { "There are conference packet assignments but no conference specified" }

        val gameAssignments = assignments.filter { it.type == ApiPacketAssignment.Type.NON_CONFERENCE_GAME }
        val games = nonConferenceGameRepo.findByIdIn(gameAssignments.map(ApiPacketAssignment::id))
        require(games.size == gameAssignments.size && games.all { it.bookingId == booking.id }) { "Invalid assignments for non-conference games" }
        val gamesById: Map<Long, NonConferenceGame> = games.associateBy { it.id!! }

        val packetIds = assignments.map { it.packetId!! }
        require(packetIds.size == packetIds.distinct().size) { "Repeated packets" }
        val packets = packetRepo.findByIdIn(packetIds)
        require(packets.size == packetIds.size) { "Invalid packet IDs" }
        require(packets.all { it.availableForCompetition == true }) { "Packets not available for competition" }
        val packetsById: Map<Long, Packet> = packets.associateBy { it.id!! }

        require(assignments.all { UNDERSTOOD_TYPES.contains(it.type) }) { "Unknown type" }

        for (assignment in assignments) {
            val packet = packetsById[assignment.packetId]!!

            when (assignment.type) {
                ApiPacketAssignment.Type.CONFERENCE -> assignConferencePacket(conference!!, packet)
                ApiPacketAssignment.Type.NON_CONFERENCE_GAME -> assignNonConferenceGamePacket(gamesById[assignment.id]!!, packet)
            }
        }
    }

    private fun assignConferencePacket(conference: BookingConference, packet: Packet) {
        bookingConferencePacketRepo.save(
            BookingConferencePacket(
                bookingConferenceId = conference.id!!,
                assignedPacket = packet,
            )
        )
    }

    private fun assignNonConferenceGamePacket(game: NonConferenceGame, packet: Packet) {
        game.assignedPacket = packet
        nonConferenceGameRepo.save(game)
    }

    fun unassign(booking: Booking) {
        val conference = bookingConferenceRepo.findByBookingId(booking.id!!)
        if (conference != null) {
            val packets = bookingConferencePacketRepo.findByBookingConferenceId(conference.id!!)
            bookingConferencePacketRepo.deleteAll(packets)
        }

        val games = nonConferenceGameRepo.findByBookingId(booking.id!!)
        games.forEach {
            it.assignedPacket = null
            nonConferenceGameRepo.save(it)
        }
    }

    companion object {
        val PACKET_ASSIGNMENT_PRIORITY_COMPARATOR: Comparator<Packet> = compareBy<Packet> { it.yearCode!! }
            .thenBy { it.number!! }
            .thenBy { it.id!! } // should be redundant, but we want this to be deterministic

        val ASSIGNMENT_DISPLAY_COMPARATOR: Comparator<ApiPacketAssignment> =
            compareBy<ApiPacketAssignment> { it.type } // which will put conference first
                .thenBy { it.date }
                .thenBy { it.sequence }
                .thenBy { it.hashCode() } // stable

        val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

        private val UNDERSTOOD_TYPES = setOf(ApiPacketAssignment.Type.CONFERENCE, ApiPacketAssignment.Type.NON_CONFERENCE_GAME)
    }
}
