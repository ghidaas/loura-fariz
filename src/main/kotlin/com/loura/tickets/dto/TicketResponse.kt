package com.loura.tickets.dto

import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority
import com.loura.tickets.enum.TicketStatus
import com.loura.tickets.model.Ticket

data class TicketResponse(
    val externalId: String,
    val subject: String,
    val status: TicketStatus,
    val category: TicketCategory?,
    val priority: TicketPriority?,
    val summary: String?,
)

fun Ticket.toResponse() =
    TicketResponse(
        externalId = externalId,
        subject = subject,
        status = status,
        category = category,
        priority = priority,
        summary = summary,
    )
