package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
data class Booking(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @ManyToOne var school: School? = null,
    var name: String? = null,
    var emailAddress: String? = null,
    var authority: String? = null,
    @ManyToOne @JoinColumn(name = "booking_status_code") var status: BookingStatus? = null,
    var shipDate: LocalDate? = null,
    var paymentReceivedDate: LocalDate? = null,
    @Column(name = "requests_w9") var requestsW9: Boolean? = null,
    var invoiceAlteredManually: Boolean? = null,
    var externalNote: String? = null,
    var internalNote: String? = null,
    @CreationTimestamp var createdAt: OffsetDateTime? = null,
    var creatorIpAddress: String? = null,
    var creationId: String? = null,
    @UpdateTimestamp var modifiedAt: OffsetDateTime? = null,
)
