package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.api.ApiBookingPracticePacketOrder
import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import com.reinsteinquizbowl.order.util.Config
import com.reinsteinquizbowl.order.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Service
class BookingService {
    @Autowired private lateinit var repo: BookingRepository
    @Autowired private lateinit var authorizer: BookingAuthorizer
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository
    @Autowired private lateinit var convert: Converter

    fun findOrCreateThenAuthorize(creationId: String): Booking {
        val entity = repo.findByCreationId(creationId) ?: Booking(creationId = creationId)

        authorizer.authorize(entity, creationId)

        return entity
    }

    fun findThenAuthorize(creationId: String): Booking {
        val entity = repo.findByCreationId(creationId) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking ID")

        authorizer.authorize(entity, creationId)

        return entity
    }

    fun refetch(booking: Booking) = repo.findByIdOrNull(booking.id!!)!!

    fun buildConfirmationEmailBody(booking: Booking): String {
        // This is cheating, but the API converter gathers so much useful information it's easier to start from that
        val api = convert.toApi(booking)

        val school = booking.school!!
        val data = mutableListOf<Pair<String, String?>>(
            "School" to "${school.shortName} (${school.name}, ${school.city}, ${school.state})",
            "Email address" to booking.emailAddress,
            "Authority" to (booking.authority ?: "unknown"),
            "Wants a Form W-9" to (if (booking.requestsW9 == true) "yes" else "no"),
        )

        api.conference?.let { conference ->
            val packetsPluralized = if (conference.packetsRequested == 1) "packet" else "packets"
            val packetsDescription =
                if (conference.assignedPackets?.isEmpty() == true) {
                    "not assigned yet"
                } else {
                    Util.makeEnglishList(conference.assignedPackets!!.sortedBy { it.number }.map { it.number.toString() })
                }
            data.add("Conference" to "${conference.name}: ${conference.packetsRequested} $packetsPluralized: $packetsDescription")

            val schoolNames = bookingConferenceSchoolRepo.findByBookingConferenceId(conference.id!!)
                .map { it.school!!.shortName!! }
                .sorted()
            data.add("Schools in conference" to Util.makeEnglishList(schoolNames))
        }

        api.nonConferenceGames?.let { nonConferenceGames -> // in practice, this will always be non-null, but it could be empty
            for (game in nonConferenceGames) {
                val packetDescription = if (game.assignedPacket == null) "not assigned yet" else game.assignedPacket!!.number.toString()
                val schoolShortNames = nonConferenceGameSchoolRepo.findByNonConferenceGameId(game.id!!)
                    .map { it.school!!.shortName!! }
                    .sorted()
                data.add("Non-conference game (${Util.makeEnglishList(schoolShortNames)})" to "(packet $packetDescription)")
            }
        }

        api.packetOrders?.takeIf { it.isNotEmpty() }?.let { practicePacketOrders ->
            val descriptions = practicePacketOrders
                .sortedWith(ApiBookingPracticePacketOrder.YEAR_AND_NUMBER_COMPARATOR)
                .map { "${it.packet!!.yearCode} #${it.packet!!.number}" }
            data.add("Practice packets" to Util.makeEnglishList(descriptions))
        }

        api.compilationOrders?.takeIf { it.isNotEmpty() }?.let { practiceCompilationOrders ->
            val descriptions = practiceCompilationOrders
                .sortedWith(compareBy({ it.compilation!!.sequence }))
                .map { it.compilation!!.name }
            data.add("Practice compilations" to Util.makeEnglishList(descriptions))
        }

        if (!booking.externalNote.isNullOrBlank()) {
            data.add("Note" to booking.externalNote)
        }

        val costDescription = api.cost?.let(CURRENCY_FORMATTER::format) ?: "TBD" // fallback shouldn't happen
        data.add("Total cost" to costDescription)

        val body = StringBuilder("<p>${booking.name} has submitted a new order:</p>")

        for ((key, value) in data) {
            // Some kind of escaping might be appropriate
            body.append("<p><b>$key:</b> $value</p>")
        }

        body.append("<p><a href='${Config.UI_PREFIX}/order/${booking.creationId}/invoice'>View Invoice</a></p>")
        body.append("<p><a href='${Config.UI_PREFIX}/admin/order/${booking.creationId}'>Manage Order</a></p>")

        return body.toString()
    }

    companion object {
        val DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)
    }
}
