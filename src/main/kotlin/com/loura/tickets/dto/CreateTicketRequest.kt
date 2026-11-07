package com.loura.tickets.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTicketRequest(
    @field:NotBlank(message = "Ticket external Id must not be blank")
    val externalId: String,
    @field:NotBlank(message = "Subject must not be blank")
    @field:Size(max = 255, message = "Subject must not exceed 255 characters")
    val subject: String,
    @field:NotBlank(message = "Ticket body must not be blank")
    val body: String,
)
