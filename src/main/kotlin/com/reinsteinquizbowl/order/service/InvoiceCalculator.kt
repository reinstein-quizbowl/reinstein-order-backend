package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.entity.BookingConference
import com.reinsteinquizbowl.order.entity.BookingPracticeCompilationOrder
import com.reinsteinquizbowl.order.entity.BookingPracticePacketOrder
import com.reinsteinquizbowl.order.entity.InvoiceLine
import com.reinsteinquizbowl.order.entity.NonConferenceGame
import com.reinsteinquizbowl.order.repository.BookingConferencePacketRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceRepository
import com.reinsteinquizbowl.order.repository.BookingConferenceSchoolRepository
import com.reinsteinquizbowl.order.repository.BookingPracticeCompilationOrderRepository
import com.reinsteinquizbowl.order.repository.BookingPracticePacketOrderRepository
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.InvoiceLineRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class InvoiceCalculator {
    @Autowired private lateinit var repo: BookingRepository
    @Autowired private lateinit var bookingConferenceRepo: BookingConferenceRepository
    @Autowired private lateinit var bookingConferencePacketRepo: BookingConferencePacketRepository
    @Autowired private lateinit var bookingConferenceSchoolRepo: BookingConferenceSchoolRepository
    @Autowired private lateinit var bookingPracticePacketOrderRepo: BookingPracticePacketOrderRepository
    @Autowired private lateinit var bookingPracticeCompilationOrderRepo: BookingPracticeCompilationOrderRepository
    @Autowired private lateinit var nonConferenceGameRepo: NonConferenceGameRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository
    @Autowired private lateinit var invoiceLineRepo: InvoiceLineRepository

    @Suppress("MagicNumber")
    private fun calculateCostForPacket(schoolsExposed: Long): BigDecimal = when (schoolsExposed) {
        0L -> BigDecimal("0.00")
        1L, 2L, 3L -> BigDecimal("15.00")
        4L -> BigDecimal("20.00")
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
            .sortedBy { it.date }
        for (game in games) {
            lines.add(calculateNonConferenceGameLine(game))
        }

        val practicePacketOrders = bookingPracticePacketOrderRepo.findByBookingId(booking.id!!)
            .sortedWith(BookingPracticePacketOrder.YEAR_AND_NUMBER_COMPARATOR)
        for (practicePacketOrder in practicePacketOrders) {
            /* FIXME: This needs to look at year.maximumPacketPracticeMaterialPrice and apply that if necessary.
             * We could implement that by reducing each item's price, adding a credit-back for the difference, or maybe in other ways.
             * Off the top of my head, I think the credit-back way is probably easier, but I'm not certain of that.
             */
            lines.add(calculatePracticePacketLine(practicePacketOrder))
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
                    itemId = packet.id,
                    label = "Packet ${packet.number} from ${packet.yearCode} for conference use",
                    quantity = 1,
                    unitCost = calculateCostForPacket(schoolsInConference),
                )
            }
    }

    private fun calculateNonConferenceGameLine(game: NonConferenceGame): InvoiceLine {
        val packet = game.assignedPacket!!
        return InvoiceLine(
            bookingId = null,
            itemType = "Non-conference game packet",
            itemId = packet.id,
            label = "Packet ${packet.number} from ${packet.yearCode} for non-conference use",
            quantity = 1,
            unitCost = calculateCostForPacket(nonConferenceGameSchoolRepo.countByNonConferenceGameId(game.id!!)),
        )
    }

    private fun calculatePracticePacketLine(practicePacketOrder: BookingPracticePacketOrder): InvoiceLine {
        val packet = practicePacketOrder.packet!!
        return InvoiceLine(
            bookingId = null,
            itemType = "Practice packet",
            itemId = packet.id,
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
            itemId = compilation.id,
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

        booking.cost = lines.sumOf(InvoiceLine::getTotalCost)
        repo.save(booking)
    }

    fun clear(booking: Booking) {
        val lines = invoiceLineRepo.findByBookingId(booking.id!!)
        invoiceLineRepo.deleteAll(lines)

        booking.cost = null
        repo.save(booking)
    }
}
