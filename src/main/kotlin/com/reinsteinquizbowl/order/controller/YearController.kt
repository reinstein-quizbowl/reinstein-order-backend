package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiYear
import com.reinsteinquizbowl.order.repository.YearRepository
import com.reinsteinquizbowl.order.service.Converter
import com.reinsteinquizbowl.order.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class YearController {
    @Autowired private lateinit var repo: YearRepository
    @Autowired private lateinit var convert: Converter

    @GetMapping("/years")
    fun getYears(): List<ApiYear> = repo.findAll().map(convert::toApi)

    @GetMapping("/years/current")
    fun getCurrentYear(): ApiYear? = repo.findAsOf(Util.today())?.let(convert::toApi)
}
