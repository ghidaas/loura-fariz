package com.loura.tickets.dto

import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority

data class ValidatedClassification(
    val category: TicketCategory,
    val priority: TicketPriority,
    val summary: String,
)
