package com.reinsteinquizbowl.order.api

data class ApiBookingPracticeStateSeriesOrder(
    var id: Long? = null,
    var bookingId: Long? = null,
    var stateSeries: ApiStateSeries? = null,
)
