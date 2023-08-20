package com.reinsteinquizbowl.order.api

data class ApiSchool(
    var id: Long,
    var name: String,
    var shortName: String,
    var address: String,
    var city: String,
    var state: String,
    var postalCode: String,
    var country: String,
    var latitude: Double?,
    var longitude: Double?,
    var active: Boolean,
    var coop: Boolean,
    var iesaId: String?,
    var note: String?,
)
