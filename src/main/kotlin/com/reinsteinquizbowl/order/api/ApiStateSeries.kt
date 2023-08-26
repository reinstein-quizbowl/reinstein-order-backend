package com.reinsteinquizbowl.order.api

import java.math.BigDecimal

data class ApiStateSeries(
    var id: Long,
    var name: String,
    var description: String?,
    var price: BigDecimal,
    var available: Boolean,
    var sequence: Long,
)
