package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.StateSeries
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StateSeriesRepository : CrudRepository<StateSeries, Long> {
    @Query("select ss from StateSeries ss where available = true order by sequence")
    fun findAvailable(): List<StateSeries>
}
