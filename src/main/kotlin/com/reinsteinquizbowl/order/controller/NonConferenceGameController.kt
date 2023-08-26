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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

            val game = repo.save(
                NonConferenceGame(
                    bookingId = booking.id,
                )
            )

            for (school in schools) {
                nonConferenceGameSchoolRepo.save(
                    NonConferenceGameSchool(
                        nonConferenceGame = game,
                        school = school,
                    )
                )
            }
        }

        val bookingRefetched = bookingRepo.findByIdOrNull(booking.id!!)!!

        return convert.toApi(bookingRefetched)
    }

    @PatchMapping("/bookings/{creationId}/nonConferenceGames/{gameId}")
    fun update(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @PathVariable gameId: Long,
        @RequestBody input: ApiNonConferenceGame,
    ): ApiNonConferenceGame {
        val (_, game) = findAndAuthorize(bookingCreationId, gameId)

        // FIXME authorize more
        // THINK: should we sanity-check the packet authorization?
        game.assignedPacket = input.assignedPacket?.id?.let(packetRepo::findByIdOrNull) ?: game.assignedPacket

        return convert.toApi(game)
    }

    @DeleteMapping("/bookings/{creationId}/nonConferenceGames/{gameId}")
    fun delete(
        @PathVariable(name = "creationId") bookingCreationId: String,
        @PathVariable gameId: Long,
    ) {
        val (_, game) = findAndAuthorize(bookingCreationId, gameId)

        val gameSchools = nonConferenceGameSchoolRepo.findByNonConferenceGameId(game.id!!)
        nonConferenceGameSchoolRepo.deleteAll(gameSchools)
        repo.delete(game)
    }

    private fun findAndAuthorize(bookingCreationId: String, gameId: Long): Pair<Booking, NonConferenceGame> {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val game = repo.findByIdOrNull(gameId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid game")
        if (game.bookingId != booking.id) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid game/booking combination")

        return Pair(booking, game)
    }
}
