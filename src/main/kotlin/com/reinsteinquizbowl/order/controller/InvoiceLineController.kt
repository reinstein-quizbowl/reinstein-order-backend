package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiInvoiceLine
import com.reinsteinquizbowl.order.entity.InvoiceLine
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.InvoiceLineRepository
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.service.InvoiceCalculator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class InvoiceLineController {
    @Autowired private lateinit var repo: InvoiceLineRepository
    @Autowired private lateinit var bookingRepo: BookingRepository
    @Autowired private lateinit var bookingService: BookingService
    @Autowired private lateinit var invoice: InvoiceCalculator
    @Autowired private lateinit var convert: Converter

    @GetMapping("/bookings/{bookingCreationId}/invoicePreview")
    fun getInvoicePreview(@PathVariable bookingCreationId: String): List<ApiInvoiceLine> {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val lines = invoice.calculateLines(booking)
        return lines.map(convert::toApi)
    }

    @DeleteMapping("/bookings/{bookingCreationId}/invoice")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteInvoiceLines(@PathVariable bookingCreationId: String): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        invoice.clear(booking)

        return convert.toApi(booking)
    }

    @PostMapping("/bookings/{bookingCreationId}/invoice")
    @PreAuthorize("hasAuthority('admin')")
    fun addLine(@PathVariable bookingCreationId: String, @RequestBody input: ApiInvoiceLine): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        repo.save(
            InvoiceLine(
                bookingId = booking.id,
                itemType = input.itemType,
                itemId = input.itemId,
                label = input.label,
                quantity = input.quantity,
                unitCost = input.unitCost,
                sequence = input.sequence,
            )
        )

        booking.invoiceAlteredManually = true
        bookingRepo.save(booking)

        return convert.toApi(booking)
    }

    @PatchMapping("/bookings/{bookingCreationId}/invoice/{invoiceLineId}")
    @PreAuthorize("hasAuthority('admin')")
    fun editLine(
        @PathVariable bookingCreationId: String,
        @PathVariable invoiceLineId: Long,
        @RequestBody input: ApiInvoiceLine
    ): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val line = repo.findByIdOrNull(invoiceLineId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        line.itemType = input.itemType ?: line.itemType
        line.itemId = input.itemId ?: line.itemId
        line.label = input.label ?: line.label
        line.quantity = input.quantity ?: line.quantity
        line.unitCost = input.unitCost ?: line.unitCost
        line.sequence = input.sequence
        repo.save(line)

        booking.invoiceAlteredManually = true
        bookingRepo.save(booking)

        return convert.toApi(booking)
    }

    @DeleteMapping("/bookings/{bookingCreationId}/invoice/{invoiceLineId}")
    @PreAuthorize("hasAuthority('admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteLine(@PathVariable bookingCreationId: String, @PathVariable invoiceLineId: Long): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val line = repo.findByIdOrNull(invoiceLineId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        repo.delete(line)
        // This will leave a gap in the sequences. I think that's harmless and there's no particular reason to fix it.

        booking.invoiceAlteredManually = true
        bookingRepo.save(booking)

        return convert.toApi(booking)
    }

    @PatchMapping("/bookings/{bookingCreationId}/invoice")
    @PreAuthorize("hasAuthority('admin')")
    fun reorderLines(@PathVariable bookingCreationId: String, @RequestBody newSequence: List<Long>): ApiBooking {
        val booking = bookingService.findThenAuthorize(bookingCreationId)

        val lines = repo.findByBookingId(booking.id!!)
        if (lines.size != newSequence.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected ${lines.size} line(s) but got ${newSequence.size}")
        }

        for (line in lines) {
            val i = newSequence.indexOf(line.id)
            if (i < 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No new sequence for line ${line.id}")
            }

            line.sequence = 1L + i
        }
        repo.saveAll(lines)

        booking.invoiceAlteredManually = true
        bookingRepo.save(booking)

        return convert.toApi(booking)
    }
}
