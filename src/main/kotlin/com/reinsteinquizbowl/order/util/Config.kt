package com.reinsteinquizbowl.order.util

import java.time.LocalDate
import java.time.ZoneId

// This object exists mostly to provide a centralized place documenting what environment variables are required.
@Suppress("UnusedPrivateProperty")
object Config {
    // These three aren't used in code, just in Spring configuration; they're listed here as a reminder to set them.
    private val DB_URL = System.getenv("REINSTEIN_DB_URL")
    private val DB_USERNAME = System.getenv("REINSTEIN_DB_USERNAME")
    private val DB_PASSWORD = System.getenv("REINSTEIN_DB_PASSWORD")

    val SENDGRID_API_KEY = System.getenv("REINSTEIN_SENDGRID_API_KEY")
    val UI_PREFIX = System.getenv("REINSTEIN_QUIZBOWL_UI_PREFIX")
}
