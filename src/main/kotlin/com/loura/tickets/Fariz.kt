package com.loura.tickets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class Fariz

fun main(args: Array<String>) {
    runApplication<Fariz>(*args)
}
