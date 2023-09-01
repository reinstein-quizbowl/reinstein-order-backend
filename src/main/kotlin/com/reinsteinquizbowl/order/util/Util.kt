package com.reinsteinquizbowl.order.util

import java.time.LocalDate
import java.time.ZoneId

object Util {
    val CANONICAL_TIME_ZONE: ZoneId = ZoneId.of("America/Chicago")

    fun today(): LocalDate = LocalDate.now(CANONICAL_TIME_ZONE)

    fun makeEnglishList(entries: List<String>, conjunction: String = "and"): String {
        if (entries.isEmpty()) return ""

        return when (entries.size) {
            1 -> entries.first()
            2 -> entries[0] + ' ' + conjunction + ' ' + entries[1]
            else -> {
                val punctuation = if (entries.any { it.contains(",") }) ";" else ","
                entries.subList(0, entries.size - 1)
                    .joinToString("$punctuation ") + "$punctuation " + conjunction + ' ' + entries.last()
            }
        }
    }

    fun handleDateInput(input: LocalDate?, oldValue: LocalDate?) = when {
        input == null -> oldValue
        input == Config.SENTINEL_NULL_DATE -> null
        else -> input
    }
}
