package com.reinsteinquizbowl.order.api

data class ApiBookingPracticePacketOrder(
    var id: Long? = null,
    var bookingId: Long? = null,
    var packet: ApiPacket? = null,
)
