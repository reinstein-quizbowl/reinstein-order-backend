package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.adapter.SendgridAdapter
import com.reinsteinquizbowl.order.api.ApiBooking
import com.reinsteinquizbowl.order.repository.BookingRepository
import com.reinsteinquizbowl.order.repository.BookingStatusRepository
import com.reinsteinquizbowl.order.repository.SchoolRepository
import com.reinsteinquizbowl.order.service.BookingService
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.service.InvoiceCalculator
import com.reinsteinquizbowl.order.util.Config
import com.reinsteinquizbowl.order.util.EmailAddress
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
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
    @Autowired private lateinit var sendgrid: SendgridAdapter
    @Autowired private lateinit var convert: Converter

    @GetMapping("/bookings")
    fun getByStatus(@RequestParam(name = "statusCode") statusCodes: List<String>): List<ApiBooking> {
        // FIXME authorize
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

        // FIXME authorize admin for these fields
        entity.cost = input.cost ?: entity.cost
        entity.shipDate = input.shipDate ?: entity.shipDate
        entity.paymentReceivedDate = input.paymentReceivedDate ?: entity.paymentReceivedDate
        entity.internalNote = input.internalNote ?: entity.internalNote

        // FIXME authorize admin in some cases only
        entity.status = input.statusCode?.let(statusRepo::findByIdOrNull) ?: entity.status ?: statusRepo.findByIdOrNull(ApiBooking.BookingStatus.UNSUBMITTED)

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

        val confirmationBody = service.buildConfirmationEmailBody(entity)

        val to = EmailAddress(Config.SUBMISSION_EMAIL_TO, Config.SUBMISSION_EMAIL_TO_DESCRIPTION)
        val cc = Config.SUBMISSION_EMAIL_CC.takeIf { !it.isNullOrBlank() }?.let { EmailAddress(it, Config.SUBMISSION_EMAIL_CC_DESCRIPTION) }

        @Suppress("TooGenericExceptionCaught")
        try {
            sendgrid.sendHtmlEmail(
                from = SUBMISSION_NOTIFICATION_EMAIL_FROM,
                to = to,
                cc = cc,
                subject = "Order from ${entity.name} (${entity.school!!.shortName})",
                bodyHtml = confirmationBody,
            )
        } catch (ex: RuntimeException) {
            System.err.println("Couldn't send a confirmation email from booking ${entity.id}: ${ex.message}\n${ex.stackTraceToString()}")
            // but we don't want the transaction rolled back or anything
        }

        return convert.toApi(entity)
    }

    companion object {
        private val SUBMISSION_NOTIFICATION_EMAIL_FROM = EmailAddress("jonah@jonahgreenthal.com", "Reinstein QuizBowl")
    }
}
