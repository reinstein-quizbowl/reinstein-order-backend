package com.reinsteinquizbowl.order.repository

import com.reinsteinquizbowl.order.entity.NonConferenceGameSchool
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NonConferenceGameSchoolRepository : CrudRepository<NonConferenceGameSchool, Long> {
    fun findByNonConferenceGameId(nonConferenceGameId: Long): List<NonConferenceGameSchool>
    fun countByNonConferenceGameId(nonConferenceGameId: Long): Long

    @Query(
        "SELECT school_id FROM non_conference_game_school WHERE non_conference_game_id = ?1",
        nativeQuery = true
    )
    fun findSchoolIdsByNonConferenceGameId(nonConferenceGameId: Long): List<Long>

    fun findByNonConferenceGameIdIn(nonConferenceGameIds: List<Long>): List<NonConferenceGameSchool>
}
