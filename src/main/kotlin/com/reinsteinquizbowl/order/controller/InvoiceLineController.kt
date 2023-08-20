package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.api.ApiInvoiceLine
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.service.InvoiceCalculator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class InvoiceLineController {
    @Autowired private lateinit var bookingService: BookingService
    @Autowired private lateinit var invoice: InvoiceCalculator
    @Autowired private lateinit var convert: Converter

    @GetMapping("/bookings/{creationId}/invoicePreview")
    fun getInvoicePreview(@PathVariable creationId: String): List<ApiInvoiceLine> {
        val booking = bookingService.findThenAuthorize(creationId)

        val lines = invoice.calculateLines(booking)
        return lines.map(convert::toApi)
    }

    @DeleteMapping("/bookings/{creationId}/invoice")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteInvoiceLines(@PathVariable creationId: String): ApiBooking {
        val booking = bookingService.findThenAuthorize(creationId)

        invoice.clear(booking)

        return convert.toApi(booking)
    }
}
