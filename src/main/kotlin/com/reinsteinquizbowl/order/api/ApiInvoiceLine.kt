package com.reinsteinquizbowl.order.api

import java.math.BigDecimal

data class ApiInvoiceLine(
    var id: Long? = null,
    var bookingId: Long? = null,
    var itemType: String? = null,
    var itemId: String? = null,
    var label: String? = null,
    var quantity: Int? = null,
    var unitCost: BigDecimal? = null,
    var sequence: Long? = null,
)
