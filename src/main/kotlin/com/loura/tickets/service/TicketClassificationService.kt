package com.loura.tickets.service

import com.loura.tickets.model.Ticket
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TicketClassificationService(
    private val ticketService: TicketService,
    private val mlClient: MlClient,
    private val validator: ClassificationValidator,
    private val injectionDetector: InjectionDetector,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun classify(tickets: List<Ticket>) {
        tickets.forEach { ticket ->
            if (!injectionDetector.isSafe("${ticket.subject}\n${ticket.body}")) {
                log.warn("Ticket {} looks like a prompt-injection attempt; marking FAILED", ticket.externalId)
                ticketService.markFailed(ticket.id)
                return@forEach
            }

            try {
                val rawOutput = mlClient.classify(ticket)
                val classification = validator.validate(rawOutput)
                ticketService.markClassified(ticket.id, classification)
            } catch (ex: Exception) {
                log.warn("Classification failed for ticket {}: {}", ticket.externalId, ex.message)
                ticketService.handleClassificationFailure(ticket.id)
            }
        }
    }
}
