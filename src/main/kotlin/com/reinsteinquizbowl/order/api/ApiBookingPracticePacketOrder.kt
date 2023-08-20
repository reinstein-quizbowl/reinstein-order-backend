package com.reinsteinquizbowl.order.api

data class ApiBookingPracticePacketOrder(
    var id: Long? = null,
    var bookingId: Long? = null,
    var packet: ApiPacket? = null,
) {
    companion object {
        val YEAR_AND_NUMBER_COMPARATOR = compareBy<ApiBookingPracticePacketOrder>({ it.packet!!.yearCode }, { it.packet!!.number })
    }
}
