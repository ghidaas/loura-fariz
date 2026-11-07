package com.loura.tickets.service

import com.loura.tickets.model.Ticket
import org.springframework.stereotype.Service
import kotlin.random.Random

/** Fake LLM: returns raw text like a real model, and 1 in 5 answers is broken on purpose. */
@Service
class MlClient {
    fun classify(ticket: Ticket): String {
        if (Random.nextInt(5) == 0) {
            return listOf(
                "This looks like a billing issue.",
                """{"category": "refunds", "priority": "urgent", "summary": "Customer wants a refund."}""",
            ).random()
        }

        val text = "${ticket.subject} ${ticket.body}".lowercase()
        val category =
            when {
                "invoice" in text || "payment" in text -> "billing"
                "password" in text || "login" in text -> "account"
                "error" in text || "crash" in text -> "technical"
                else -> "other"
            }
        val priority = if ("urgent" in text || "cannot" in text) "high" else "medium"
        val summary = "Customer reports: ${ticket.subject.replace("\"", "'")}"

        return """{"category": "$category", "priority": "$priority", "summary": "$summary"}"""
    }
}
