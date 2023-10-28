package com.reinsteinquizbowl.order.api

data class ApiPacketExposure(
    val packetId: Long,
    val exposedSchoolId: Long,
    val source: String,
    val sourceId: Long,
    val bookingId: Long,
    val bookingCreationId: String,
    val ordererSchoolId: Long?,
    val tentativePacketExposure: Boolean,
    val confirmedPacketExposure: Boolean,
)
