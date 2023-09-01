package com.reinsteinquizbowl.order.service

import com.reinsteinquizbowl.order.entity.Year
import com.reinsteinquizbowl.order.repository.YearRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class YearService {
    @Autowired private lateinit var yearRepo: YearRepository

    fun getYear(yearCode: String?, required: Boolean): Year? {
        if (yearCode.isNullOrBlank()) {
            if (required) {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "Missing year")
            } else {
                return null
            }
        }

        return yearRepo.findByIdOrNull(yearCode) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid year")
    }
}
