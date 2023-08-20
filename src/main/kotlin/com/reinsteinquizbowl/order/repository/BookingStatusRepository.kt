package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.BookingStatus
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BookingStatusRepository : CrudRepository<BookingStatus, String>
