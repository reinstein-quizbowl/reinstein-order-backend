package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.NonConferenceGame
import com.reinsteinquizbowl.order.repository.NonConferenceGameRepository
import com.reinsteinquizbowl.order.repository.NonConferenceGameSchoolRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NonConferenceGameService {
    @Autowired private lateinit var repo: NonConferenceGameRepository
    @Autowired private lateinit var nonConferenceGameSchoolRepo: NonConferenceGameSchoolRepository

    fun delete(game: NonConferenceGame) {
        val schools = nonConferenceGameSchoolRepo.findByNonConferenceGameId(game.id!!)
        nonConferenceGameSchoolRepo.deleteAll(schools)

        repo.delete(game)
    }
}
