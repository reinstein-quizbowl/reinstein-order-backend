package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiStateSeries
import com.reinsteinquizbowl.order.repository.StateSeriesRepository
import com.reinsteinquizbowl.order.service.Converter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class StateSeriesController {
    @Autowired private lateinit var repo: StateSeriesRepository
    @Autowired private lateinit var convert: Converter

    private val logger = LoggerFactory.getLogger(StateSeriesController::class.java)

    @GetMapping("/stateSeries")
    fun getStateSeries(@RequestParam filter: String? = null): List<ApiStateSeries> {
        val stateSeries = when (filter) {
            "available" -> repo.findAvailable()
            "all" -> repo.findAll()
            else -> {
                logger.warn("Unknown filter: $filter")
                repo.findAvailable()
            }
        }

        return stateSeries.map(convert::toApi)
    }
}
