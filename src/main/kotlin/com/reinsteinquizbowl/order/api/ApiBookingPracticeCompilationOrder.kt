package com.reinsteinquizbowl.order.api

data class ApiBookingPracticeCompilationOrder(
    var id: Long? = null,
    var bookingId: Long? = null,
    var compilation: ApiCompilation? = null,
)
