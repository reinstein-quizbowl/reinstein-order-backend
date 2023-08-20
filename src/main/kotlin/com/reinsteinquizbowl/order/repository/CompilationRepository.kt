package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.Compilation
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CompilationRepository : CrudRepository<Compilation, Long> {
    @Query("select c from Compilation c where available = true order by sequence")
    fun findAvailable(): List<Compilation>
}
