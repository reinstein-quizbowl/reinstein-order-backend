package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiCompilation
import com.reinsteinquizbowl.order.repository.CompilationRepository
import com.reinsteinquizbowl.order.service.Converter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CompilationController {
    @Autowired private lateinit var repo: CompilationRepository
    @Autowired private lateinit var convert: Converter

    @GetMapping("/compilations")
    fun getCompilations(): List<ApiCompilation> = repo.findAvailable().map(convert::toApi)
}
