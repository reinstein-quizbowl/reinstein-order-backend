package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
data class BookingPracticeStateSeriesOrder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var bookingId: Long? = null,
    @ManyToOne var stateSeries: StateSeries? = null,
)
