package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.adapter.SendgridAdapter
import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.entity.Booking
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.BookingStatusRepository
import com.reinsteinquizbowl.order.repository.SchoolRepository
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.service.InvoiceCalculator
import com.reinsteinquizbowl.order.spring.UserService
import com.reinsteinquizbowl.order.util.Config
import com.reinsteinquizbowl.order.util.EmailAddress
import com.reinsteinquizbowl.order.util.Util
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class BookingController {
    @Autowired private lateinit var repo: BookingRepository
    @Autowired private lateinit var schoolRepo: SchoolRepository
    @Autowired private lateinit var statusRepo: BookingStatusRepository
    @Autowired private lateinit var service: BookingService
    @Autowired private lateinit var invoice: InvoiceCalculator
    @Autowired private lateinit var user: UserService
    @Autowired private lateinit var sendgrid: SendgridAdapter
    @Autowired private lateinit var convert: Converter

    private val logger = LoggerFactory.getLogger(BookingController::class.java)

    @GetMapping("/bookings")
    @PreAuthorize("hasAuthority('admin')")
    fun getByStatus(@RequestParam(name = "statusCode") statusCodes: List<String>): List<ApiBooking> {
        return repo.findByStatusCodeIn(statusCodes).map(convert::toApi)
    }

    @GetMapping("/bookings/{creationId}")
    fun getBooking(@PathVariable creationId: String): ApiBooking? {
        val entity = repo.findByCreationId(creationId) ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking ID")
        return convert.toApi(entity)
    }

    @PostMapping("/bookings/{creationId}")
    fun upsert(
        @PathVariable creationId: String,
        @RequestBody input: ApiBooking,
        response: HttpServletResponse,
    ): ApiBooking {
        val entity = service.findOrCreateThenAuthorize(creationId)
        val isNew = entity.id == null

        entity.school = input.school?.id?.let(schoolRepo::findByIdOrNull) ?: entity.school
        entity.school ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "School is required")

        entity.name = input.name ?: entity.name
        entity.emailAddress = input.emailAddress ?: entity.emailAddress
        entity.authority = input.authority ?: entity.authority
        entity.requestsW9 = input.requestsW9 ?: entity.requestsW9 ?: false
        entity.externalNote = input.externalNote ?: entity.externalNote

        if (user.isAdmin()) {
            entity.shipDate = Util.handleDateInput(input.shipDate, entity.shipDate)
            entity.paymentReceivedDate = Util.handleDateInput(input.paymentReceivedDate, entity.paymentReceivedDate)
            entity.internalNote = input.internalNote ?: entity.internalNote
            entity.status = input.statusCode?.let(statusRepo::findByIdOrNull) ?: entity.status ?: statusRepo.findByIdOrNull(ApiBooking.BookingStatus.UNSUBMITTED)
        } else {
            if (entity.status != null && entity.status!!.code != ApiBooking.BookingStatus.UNSUBMITTED) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking was already submitted")
            }
            entity.status = statusRepo.findByIdOrNull(ApiBooking.BookingStatus.UNSUBMITTED)
        }

        val saved = repo.save(entity)

        response.status = if (isNew) HttpStatus.CREATED.value() else HttpStatus.ACCEPTED.value()

        return convert.toApi(saved)
    }

    @PostMapping("/bookings/{creationId}/submit")
    fun submit(@PathVariable creationId: String): ApiBooking {
        val entity = service.findThenAuthorize(creationId)

        require(entity.status?.code == ApiBooking.BookingStatus.UNSUBMITTED) { "Already submitted" }

        invoice.calculateAndAttach(entity)
        entity.status = statusRepo.findByIdOrNull(ApiBooking.BookingStatus.SUBMITTED)!!
        repo.save(entity)

        sendInternalConfirmationEmail(entity)
        sendExternalConfirmationEmail(entity)

        return convert.toApi(entity)
    }

    @PostMapping("/bookings/{creationId}/recalculateInvoice")
    @PreAuthorize("hasAuthority('admin')")
    fun recalculateInvoice(@PathVariable creationId: String): ApiBooking {
        val entity = service.findThenAuthorize(creationId)

        invoice.calculateAndAttach(entity)

        return convert.toApi(service.refetch(entity))
    }

    @PostMapping("/bookings/{creationId}/confirm")
    @PreAuthorize("hasAuthority('admin')")
    fun confirm(@PathVariable creationId: String): ApiBooking {
        val entity = service.findThenAuthorize(creationId)

        sendInternalConfirmationEmail(entity)
        sendExternalConfirmationEmail(entity)

        return convert.toApi(entity)
    }

    private fun sendInternalConfirmationEmail(booking: Booking) {
        val body = service.buildInternalConfirmationEmailBody(booking)

        val to = EmailAddress(Config.SUBMISSION_EMAIL_TO, Config.SUBMISSION_EMAIL_TO_DESCRIPTION)
        val cc = Config.SUBMISSION_EMAIL_CC.takeIf { !it.isNullOrBlank() }?.let { EmailAddress(it, Config.SUBMISSION_EMAIL_CC_DESCRIPTION) }

        @Suppress("TooGenericExceptionCaught")
        try {
            sendgrid.sendHtmlEmail(
                from = Config.FROM_ADDRESS,
                to = to,
                cc = listOfNotNull(cc),
                subject = "Order from ${booking.name} (${booking.school!!.shortName})",
                bodyHtml = body,
            )
        } catch (ex: RuntimeException) {
            logger.warn("Couldn't send internal confirmation email from booking ${booking.id}", ex)
            // but we don't want the transaction rolled back or anything
        }
    }

    private fun sendExternalConfirmationEmail(booking: Booking) {
        if (booking.emailAddress.isNullOrBlank()) {
            logger.warn("No email address for booking ${booking.id}; can't send an external confirmation email")
            return
        }

        val internalConfirmationBody = service.buildExternalConfirmationEmailBody(booking)

        val internalRecipient = EmailAddress(Config.SUBMISSION_EMAIL_TO, Config.SUBMISSION_EMAIL_TO_DESCRIPTION)
        val cc = Config.SUBMISSION_EMAIL_CC.takeIf { !it.isNullOrBlank() }?.let { EmailAddress(it, Config.SUBMISSION_EMAIL_CC_DESCRIPTION) }

        @Suppress("TooGenericExceptionCaught")
        try {
            sendgrid.sendHtmlEmail(
                from = Config.FROM_ADDRESS,
                to = EmailAddress(address = booking.emailAddress!!, description = booking.name),
                cc = listOfNotNull(internalRecipient, cc),
                subject = "Your Order with Reinstein QuizBowl",
                bodyHtml = internalConfirmationBody,
            )
        } catch (ex: RuntimeException) {
            logger.warn("Couldn't send external confirmation email from booking ${booking.id}", ex)
            // but we don't want the transaction rolled back or anything
        }
    }
}
