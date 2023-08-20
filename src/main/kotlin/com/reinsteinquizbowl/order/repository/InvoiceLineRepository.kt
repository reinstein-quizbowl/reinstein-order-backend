package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.InvoiceLine
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface InvoiceLineRepository : CrudRepository<InvoiceLine, Long> {
    fun findByBookingId(bookingId: Long): List<InvoiceLine>
}
