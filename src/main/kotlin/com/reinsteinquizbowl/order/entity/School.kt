package com.reinsteinquizbowl.order.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class School(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    var name: String? = null,
    var shortName: String? = null,
    var address: String? = null,
    var city: String? = null,
    var state: String? = null,
    var postalCode: String? = null,
    var country: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var active: Boolean? = null,
    var coop: Boolean? = null,
    var iesaId: String? = null,
    var note: String? = null,
)
