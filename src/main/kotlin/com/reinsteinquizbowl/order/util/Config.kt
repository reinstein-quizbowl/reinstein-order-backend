package com.reinsteinquizbowl.order.util

// This object exists mostly to provide a centralized place documenting what environment variables are required, but it also contains some non-environment variables.
@Suppress("UnusedPrivateProperty")
object Config {
    // The ones in this group aren't used in code, just in Spring configuration; they're listed here as a reminder to set them.
    private val SERVER_PORT = System.getenv("REINSTEIN_SERVER_PORT")
    private val DB_URL = System.getenv("REINSTEIN_DB_URL")
    private val DB_USERNAME = System.getenv("REINSTEIN_DB_USERNAME")
    private val DB_PASSWORD = System.getenv("REINSTEIN_DB_PASSWORD")

    val SENDGRID_API_KEY = System.getenv("REINSTEIN_SENDGRID_API_KEY")
    val UI_PREFIX = System.getenv("REINSTEIN_QUIZBOWL_UI_PREFIX")

    val SUBMISSION_EMAIL_TO = System.getenv("REINSTEIN_SUBMISSION_EMAIL_TO")
    val SUBMISSION_EMAIL_TO_DESCRIPTION = System.getenv("REINSTEIN_SUBMISSION_EMAIL_TO_DESCRIPTION")
    val SUBMISSION_EMAIL_CC: String? = System.getenv("REINSTEIN_SUBMISSION_EMAIL_CC")
    val SUBMISSION_EMAIL_CC_DESCRIPTION: String? = System.getenv("REINSTEIN_SUBMISSION_EMAIL_CC_DESCRIPTION")

    val FROM_ADDRESS = EmailAddress(address = "david@reinsteinquizbowl.com", description = "David Reinstein")
}
