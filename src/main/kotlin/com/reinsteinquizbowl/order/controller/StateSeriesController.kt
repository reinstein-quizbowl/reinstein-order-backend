package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiStateSeries
import com.reinsteinquizbowl.order.repository.StateSeriesRepository
import com.reinsteinquizbowl.order.service.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StateSeriesController {
    @Autowired private lateinit var repo: StateSeriesRepository
    @Autowired private lateinit var convert: Converter

    @GetMapping("/stateSeries")
    fun getStateSeries(): List<ApiStateSeries> = repo.findAvailable().map(convert::toApi)
}
