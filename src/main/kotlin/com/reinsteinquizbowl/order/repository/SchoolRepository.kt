package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.School
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SchoolRepository : CrudRepository<School, Long> {
    @Query("select s from School s where active = true")
    fun findActive(): List<School>

    fun findByIdIn(ids: List<Long>): List<School>
}
