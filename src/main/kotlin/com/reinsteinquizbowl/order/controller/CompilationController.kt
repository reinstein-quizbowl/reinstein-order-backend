package com.reinsteinquizbowl.order.controller

import com.reinsteinquizbowl.order.api.ApiCompilation
import com.reinsteinquizbowl.order.repository.CompilationRepository
import com.reinsteinquizbowl.order.service.Converter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CompilationController {
    @Autowired private lateinit var repo: CompilationRepository
    @Autowired private lateinit var convert: Converter

    private val logger = LoggerFactory.getLogger(CompilationController::class.java)

    @GetMapping("/compilations")
    fun getCompilations(@RequestParam filter: String? = null): List<ApiCompilation> {
        val compilations = when (filter) {
            "available" -> repo.findAvailable()
            "all" -> repo.findAll()
            else -> {
                logger.warn("Unknown filter: $filter")
                repo.findAvailable()
            }
        }

        return compilations.map(convert::toApi)
    }
}
