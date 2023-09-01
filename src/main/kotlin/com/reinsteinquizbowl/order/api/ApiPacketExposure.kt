package com.reinsteinquizbowl.order.api

data class ApiPacketExposure(
    var packetId: Long,
    var exposedSchoolId: Long,
    var source: String,
    var bookingId: Long,
    var ordererSchoolId: Long,
    var tentativePacketExposure: Boolean,
    var confirmedPacketExposure: Boolean,
)
