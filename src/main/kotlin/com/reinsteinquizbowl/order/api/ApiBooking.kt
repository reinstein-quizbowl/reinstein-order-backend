package com.reinsteinquizbowl.order.api

import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class ApiBooking(
    var id: Long? = null,
    var school: ApiSchool? = null,
    var name: String? = null,
    var emailAddress: String? = null,
    var authority: String? = null,
    var invoiceLabel: String? = null,
    var cost: BigDecimal? = null,
    var statusCode: String? = null,
    var shipDate: LocalDate? = null,
    var paymentReceivedDate: LocalDate? = null,
    var requestsW9: Boolean? = null,
    var externalNote: String? = null,
    var internalNote: String? = null,

    var conference: ApiBookingConference? = null, // the data model supports multiple conferences per order, but the API layer doesn't for now because we don't anticipate needing it
    var nonConferenceGames: List<ApiNonConferenceGame>? = null,
    var packetOrders: List<ApiBookingPracticePacketOrder>? = null,
    var compilationOrders: List<ApiBookingPracticeCompilationOrder>? = null,
    var invoiceLines: List<ApiInvoiceLine>? = null,

    var createdAt: OffsetDateTime? = null,
    var creationId: String? = null,
    var modifiedAt: OffsetDateTime? = null,
    // creatorIpAddress is deliberately omitted
) {
    object BookingStatus {
        const val UNSUBMITTED = "unsubmitted"
        const val SUBMITTED = "submitted"
        const val APPROVED = "approved"
        const val SHIPPED = "shipped"
        const val CANCELED = "canceled"
        const val REJECTED = "rejected"

        val VALID_VALUES = listOf(UNSUBMITTED, SUBMITTED, APPROVED, SHIPPED, CANCELED, REJECTED)
    }
}
