package com.loura.tickets.worker

import com.loura.tickets.service.TicketClassificationService
import com.loura.tickets.service.TicketService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TicketClassificationWorker(
    private val ticketService: TicketService,
    private val classificationService: TicketClassificationService,
) {
    @Scheduled(cron = "0 */10 * * * *") // Every 10 minutes
    fun processPendingTickets() {
        val tickets = ticketService.getAllPendingTickets()
        classificationService.classify(tickets)
    }
}
