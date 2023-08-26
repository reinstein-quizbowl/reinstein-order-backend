package com.reinsteinquizbowl.order.api

import java.time.LocalDate

data class ApiNonConferenceGame(
    var id: Long? = null,
    var bookingId: Long? = null,
    var schoolIds: List<Long>? = null,
    var assignedPacket: ApiPacket? = null,
)
