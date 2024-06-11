package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.entity.BookingPracticeCompilationOrder
import com.reinsteinquizbowl.order.entity.BookingPracticePacketOrder
import com.reinsteinquizbowl.order.entity.BookingPracticeStateSeriesOrder
import com.reinsteinquizbowl.order.entity.InvoiceLine
import com.reinsteinquizbowl.order.entity.NonConferenceGame
import com.reinsteinquizbowl.order.entity.Packet
import com.reinsteinquizbowl.order.entity.Year
import com.reinsteinquizbowl.order.repository.BookingConferencePacketRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeCompilationOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticePacketOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeStateSeriesOrderRepository
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.InvoiceLineRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import com.reinsteinquizbowl.order.repository.YearRepository
import com.reinsteinquizbowl.order.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class InvoiceCalculator {
    @Autowired private lateinit var repo: BookingRepository
    @Autowired private lateinit var bookingConferenceRepo: BookingConferenceRepository
    @Autowired private lateinit var bookingConferencePacketRepo: BookingConferencePacketRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var bookingPracticeStateSeriesOrderRepo: BookingPracticeStateSeriesOrderRepository
    @Autowired private lateinit var bookingPracticePacketOrderRepo: BookingPracticePacketOrderRepository
    @Autowired private lateinit var bookingPracticeCompilationOrderRepo: BookingPracticeCompilationOrderRepository
    @Autowired private lateinit var nonConferenceGameRepo: NonConferenceGameRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository
    @Autowired private lateinit var yearRepo: YearRepository
    @Autowired private lateinit var invoiceLineRepo: InvoiceLineRepository

    fun determineInvoiceLabel(booking: Booking): String {
        val schoolId = booking.school!!.id!!

        val bookingsWithLowerId = repo.countLowerIdBookingsForSchoolId(schoolId, booking.id!!)
        val thisBookingSequence = bookingsWithLowerId + 1

        return "$schoolId-$thisBookingSequence"
    }

    // This logic is described in the UI, so when updating it, it should be updated in both places.
    @Suppress("MagicNumber")
    private fun calculateCostForPacket(schoolsExposed: Long): BigDecimal = when (schoolsExposed) {
        0L -> BigDecimal("0.00")
        1L, 2L, 3L, 4L -> BigDecimal("20.00")
        5L -> BigDecimal("25.00")
        else -> BigDecimal("30.00")
    }

    /* This method does not attach the lines to the booking, so it can be used to calculate a preview.
     * If you want to save the lines on the invoice, use calculateAndAttachInvoiceLines().
     */
    fun calculateLines(booking: Booking): List<InvoiceLine> {
        val lines = mutableListOf<InvoiceLine>()

        bookingConferenceRepo.findByBookingId(booking.id!!)?.let { conference ->
            lines.addAll(calculateConferenceLines(conference))
        }

        val games = nonConferenceGameRepo.findByBookingId(booking.id!!)
            .filter { it.assignedPacket != null }
            .sortedBy { it.id }
        val nonConferenceGamePackets: List<Packet> = games
            .mapNotNull(NonConferenceGame::assignedPacket)
            .distinctBy { it.id!! }
            .sortedWith(compareBy(Packet::yearCode, Packet::number))
        val nonConferenceGamesByPacketId: Map<Long, List<NonConferenceGame>> = games
            .sortedBy { it.id!! }
            .groupBy { it.assignedPacket!!.id!! }
        for (packet in nonConferenceGamePackets) {
            val gamesThisPacket = nonConferenceGamesByPacketId[packet.id!!] ?: emptyList()
            if (gamesThisPacket.isNotEmpty()) {
                lines.add(calculateNonConferenceGameLine(packet, gamesThisPacket))
            }
        }

        val practiceStateSeriesOrders = bookingPracticeStateSeriesOrderRepo.findByBookingId(booking.id!!)
            .sortedBy { it.stateSeries!!.sequence }
        for (practiceStateSeriesOrder in practiceStateSeriesOrders) {
            lines.add(calculatePracticeStateSeriesLine(practiceStateSeriesOrder))
        }

        val practicePacketOrdersByYearCode: Map<String, List<BookingPracticePacketOrder>> = bookingPracticePacketOrderRepo.findByBookingId(booking.id!!)
            .groupBy { it.packet!!.yearCode!! }
        val yearsByCode: Map<String, Year> = yearRepo.findAllById(practicePacketOrdersByYearCode.keys)
            .associateBy { it.code!! }
        for ((yearCode, orders) in practicePacketOrdersByYearCode) {
            val year = yearsByCode[yearCode]!!

            val practicePacketLines = orders.sortedWith(BookingPracticePacketOrder.YEAR_AND_NUMBER_COMPARATOR)
                .map(this::calculatePracticePacketLine)

            lines.addAll(practicePacketLines)

            val total = practicePacketLines.sumOf(InvoiceLine::getTotalCost)
            val max = year.maximumPacketPracticeMaterialPrice
            if (max != null && total > max) {
                lines.add(
                    InvoiceLine(
                        bookingId = null,
                        itemType = "Practice packet discount",
                        itemId = year.code,
                        label = "Practice-packet discount for ${year.name}",
                        quantity = 1,
                        unitCost = max - total,
                    )
                )
            }
        }

        val practiceCompilationOrders = bookingPracticeCompilationOrderRepo.findByBookingId(booking.id!!)
            .sortedBy { it.compilation!!.sequence }
        for (practiceCompilationOrder in practiceCompilationOrders) {
            lines.add(calculatePracticeCompilationLine(practiceCompilationOrder))
        }

        lines.forEachIndexed { index, line -> line.sequence = index + 1L }

        return lines
    }

    private fun calculateConferenceLines(conference: BookingConference): List<InvoiceLine> {
        val conferencePackets = bookingConferencePacketRepo.findByBookingConferenceId(conference.id!!)
            .sortedBy { it.assignedPacket?.number }
        val schoolsInConference = bookingConferenceSchoolRepo.countByBookingConferenceId(conference.id!!)

        return conferencePackets.filter { it.assignedPacket != null }
            .map { conferencePacketAssignment ->
                val packet = conferencePacketAssignment.assignedPacket!!
                InvoiceLine(
                    bookingId = null,
                    itemType = "Conference packet",
                    itemId = packet.id!!.toString(),
                    label = "${packet.getName()} for conference use",
                    quantity = 1,
                    unitCost = calculateCostForPacket(schoolsInConference),
                )
            }
    }

    private fun calculateNonConferenceGameLine(packet: Packet, games: List<NonConferenceGame>): InvoiceLine {
        val schoolNames = mutableListOf<List<String>>()
        for (game in games) {
            // This could be optimized into one database query if really necessary
            schoolNames.add(nonConferenceGameSchoolRepo.findByNonConferenceGameId(game.id!!).map { it.school!!.shortName!! })
        }
        val schoolCount = schoolNames.flatten().size.toLong()
        val gameDescriptions: List<String> = schoolNames.map { "the non-conference game involving ${Util.makeEnglishList(it)}" }

        return InvoiceLine(
            bookingId = null,
            itemType = "Non-conference game packet",
            itemId = packet.id!!.toString(),
            label = "${packet.getName()} for ${Util.makeEnglishList(gameDescriptions)}",
            quantity = 1,
            unitCost = calculateCostForPacket(schoolCount),
        )
    }

    private fun calculatePracticeStateSeriesLine(practiceStateSeriesOrder: BookingPracticeStateSeriesOrder): InvoiceLine {
        val stateSeries = practiceStateSeriesOrder.stateSeries!!
        return InvoiceLine(
            bookingId = null,
            itemType = "Practice State Series",
            itemId = stateSeries.id!!.toString(),
            label = "${stateSeries.name} for practice use",
            quantity = 1,
            unitCost = stateSeries.price,
        )
    }

    private fun calculatePracticePacketLine(practicePacketOrder: BookingPracticePacketOrder): InvoiceLine {
        val packet = practicePacketOrder.packet!!
        return InvoiceLine(
            bookingId = null,
            itemType = "Practice packet",
            itemId = packet.id!!.toString(),
            label = "Packet ${packet.number} from ${packet.yearCode} for practice use",
            quantity = 1,
            unitCost = packet.priceAsPracticeMaterial,
        )
    }

    private fun calculatePracticeCompilationLine(practiceCompilationOrder: BookingPracticeCompilationOrder): InvoiceLine {
        val compilation = practiceCompilationOrder.compilation!!
        return InvoiceLine(
            bookingId = null,
            itemType = "Practice compilation",
            itemId = compilation.id!!.toString(),
            label = "${compilation.name} compilation for practice use",
            quantity = 1,
            unitCost = compilation.price,
        )
    }

    fun calculateAndAttach(booking: Booking) {
        clear(booking)

        val lines = calculateLines(booking)
        lines.forEach { it.bookingId = booking.id!! }
        invoiceLineRepo.saveAll(lines)

        booking.invoiceAlteredManually = false

        repo.save(booking)
    }

    fun clear(booking: Booking) {
        val lines = invoiceLineRepo.findByBookingId(booking.id!!)
        invoiceLineRepo.deleteAll(lines)

        booking.invoiceAlteredManually = false
    }
}
