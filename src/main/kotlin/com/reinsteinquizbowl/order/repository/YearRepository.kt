package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.Year
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface YearRepository : CrudRepository<Year, String> {
    @Query("select y from Year y where y.startDate <= ?1 and ?1 < y.endDate")
    fun findAsOf(date: LocalDate): Year?
}
