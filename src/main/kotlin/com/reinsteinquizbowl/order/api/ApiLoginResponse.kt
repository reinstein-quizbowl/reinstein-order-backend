package com.reinsteinquizbowl.order.api

import java.time.Instant

data class ApiLoginResponse(
    val username: String,
    val roles: List<String>,
    val token: String,
    val tokenExpires: Instant, // technically redundant as the information is embedded in `token`, but it's useful and efficient to have it available without parsing the token
)
