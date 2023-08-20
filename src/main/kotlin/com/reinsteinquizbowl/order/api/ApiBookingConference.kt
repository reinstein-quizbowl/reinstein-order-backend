package com.reinsteinquizbowl.order.api

data class ApiBookingConference(
    var id: Long? = null,
    var bookingId: Long? = null,
    var name: String? = null,
    var packetsRequested: Int? = null,
    var schoolIds: List<Long>? = null,
    var assignedPackets: List<ApiPacket>? = null,
)
