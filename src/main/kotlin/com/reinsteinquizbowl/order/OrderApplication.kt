package com.reinsteinquizbowl.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrderApplication

@Suppress("SpreadOperator") // in previous projects it just hasn't worked without it
fun main(args: Array<String>) {
    runApplication<OrderApplication>(*args)
}
