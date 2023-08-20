package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.Packet
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PacketRepository : CrudRepository<Packet, Long> {
    fun findByYearCode(yearCode: String): List<Packet>

    fun findByAvailableForCompetitionIsTrue(): List<Packet>

    // We can only find IDs because of either limits in Hibernate or limits in my understanding of Hibernate
    @Query(
        """
            select id
            from packet
            where available_for_competition = true
                and id not in (select packet_id from packet_exposure where school_id in ?1)
            order by year_code, number
        """,
        nativeQuery = true
    )
    fun findIdsOfUnexposedPacketsAvailableForCompetition(schoolIds: List<Long>): List<Long>

    fun findByIdIn(packetIds: List<Long>): List<Packet>
}
