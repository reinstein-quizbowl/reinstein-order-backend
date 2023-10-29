package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiNonConferenceGame
import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.entity.NonConferenceGame
import com.reinsteinquizbowl.order.entity.NonConferenceGameSchool
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import com.reinsteinquizbowl.order.repository.PacketRepository
import com.reinsteinquizbowl.order.repository.SchoolRepository
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.spring.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class NonConferenceGameController {
    @Autowired private lateinit var repo: NonConferenceGameRepository
    @Autowired private lateinit var bookingRepo: BookingRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository
    @Autowired private lateinit var packetRepo: PacketRepository
    @Autowired private lateinit var schoolRepo: SchoolRepository
    @Autowired private lateinit var bookingService: BookingService
    @Autowired private lateinit var user: UserService
    @Autowired private lateinit var convert: Converter

    @PostMapping("/bookings/{creationId}/nonConferenceGames")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @RequestBody input: List<ApiNonConferenceGame>,
    ): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        for (gameInput in input) {
            val schoolIds = gameInput.schoolIds
            if (schoolIds.isNullOrEmpty() || schoolIds.size < 2) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Specify at least two schools")
            val schools = schoolRepo.findByIdIn(schoolIds)
            if (schools.size != schoolIds.size) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid school ID(s)")

            val game = NonConferenceGame(bookingId = booking.id)
            gameInput.assignedPacket?.id?.let { assignedPacketId ->
                if (user.isAdmin()) {
                    val packet = packetRepo.findByIdOrNull(assignedPacketId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid packet")
                    game.assignedPacket = packet
                }
            }

            val savedGame = repo.save(game)

            for (school in schools) {
                nonConferenceGameSchoolRepo.save(
                    NonConferenceGameSchool(
                        nonConferenceGame = savedGame,
                        school = school,
                    )
                )
            }
        }

        val bookingRefetched = bookingRepo.findByIdOrNull(booking.id!!)!!

        return convert.toApi(bookingRefetched)
    }

    @DeleteMapping("/bookings/{creationId}/nonConferenceGames/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @PathVariable gameId: Long,
    ) {
        val (_, game) = findAndAuthorize(bookingCreationId, gameId)

        val gameSchools = nonConferenceGameSchoolRepo.findByNonConferenceGameId(game.id!!)
        nonConferenceGameSchoolRepo.deleteAll(gameSchools)
        repo.delete(game)
    }

    @DeleteMapping("/bookings/{creationId}/nonConferenceGames/{gameId}/packet")
    @PreAuthorize("hasAuthority('admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removePacket(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @PathVariable gameId: Long,
    ) {
        val (_, game) = findAndAuthorize(bookingCreationId, gameId)

        game.assignedPacket = null
        repo.save(game)
    }

    @PostMapping("/bookings/{creationId}/nonConferenceGames/{gameId}/packet")
    @PreAuthorize("hasAuthority('admin')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun assignPacket(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @PathVariable gameId: Long,
        @RequestParam packetId: Long,
    ): String {
        val (_, game) = findAndAuthorize(bookingCreationId, gameId)

        val packet = packetRepo.findByIdOrNull(packetId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid packet")

        game.assignedPacket = packet
        repo.save(game)

        return "{}"
    }

    private fun findAndAuthorize(bookingCreationId: String, gameId: Long): Pair<Booking, NonConferenceGame> {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val game = repo.findByIdOrNull(gameId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid game")
        if (game.bookingId != booking.id) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid game/booking combination")

        return Pair(booking, game)
    }
}
