package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiSchool
import com.reinsteinquizbowl.order.entity.School
import com.reinsteinquizbowl.order.repository.SchoolRepository
import com.reinsteinquizbowl.order.service.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SchoolController {
    @Autowired private lateinit var repo: SchoolRepository
    @Autowired private lateinit var convert: Converter

    @GetMapping("/schools")
    fun getAll(): List<ApiSchool> = repo.findAll().sortedBy(School::shortName).map(convert::toApi)
}
