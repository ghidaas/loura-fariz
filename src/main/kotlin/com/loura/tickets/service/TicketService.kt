package com.loura.tickets.service

import com.loura.tickets.dto.CreateTicketRequest
import com.loura.tickets.dto.TicketResponse
import com.loura.tickets.dto.ValidatedClassification
import com.loura.tickets.dto.toResponse
import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority
import com.loura.tickets.enum.TicketStatus
import com.loura.tickets.model.Ticket
import com.loura.tickets.repository.TicketRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Date

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
) {
    fun createTicket(request: CreateTicketRequest): TicketResponse {
        if (ticketRepository.findByExternalId(request.externalId) != null) {
            throw duplicateTicket()
        }

        val ticket =
            Ticket(
                externalId = request.externalId,
                subject = request.subject,
                body = request.body,
            )

        val savedTicket = ticketRepository.save(ticket)

        return savedTicket.toResponse()
    }

    fun getTicketByExternalId(externalId: String): TicketResponse {
        val ticket =
            ticketRepository.findByExternalId(externalId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found")
        return ticket.toResponse()
    }

    fun getTickets(
        category: TicketCategory?,
        priority: TicketPriority?,
        page: Int,
        size: Int,
    ): Page<TicketResponse> =
        ticketRepository
            .findTickets(
                category,
                priority,
                PageRequest.of(
                    page.coerceAtLeast(0),
                    size.coerceIn(1, MAX_PAGE_SIZE),
                    Sort.by(Sort.Direction.DESC, "id"),
                ),
            ).map { it.toResponse() }

    fun getAllPendingTickets(): List<Ticket> = ticketRepository.findPendingTickets()

    @Transactional
    fun markClassified(
        id: Long,
        classification: ValidatedClassification,
    ) {
        ticketRepository.updateClassification(
            id = id,
            priority = classification.priority,
            category = classification.category,
            summary = classification.summary,
            status = TicketStatus.CLASSIFIED,
        )
    }

    @Transactional
    fun markFailed(id: Long) {
        val ticket = ticketRepository.findById(id).orElseThrow()
        ticket.status = TicketStatus.FAILED
        ticket.updatedAt = Date()
        ticketRepository.save(ticket)
    }

    @Transactional
    fun handleClassificationFailure(id: Long) {
        val ticket = ticketRepository.findById(id).orElseThrow()

        ticket.attemptCount++

        if (ticket.attemptCount >= MAX_ATTEMPTS) {
            ticket.status = TicketStatus.FAILED
        } else {
            ticket.status = TicketStatus.PENDING
        }
        ticket.updatedAt = Date()

        ticketRepository.save(ticket)
    }

    private fun duplicateTicket() =
        ResponseStatusException(
            HttpStatus.CONFLICT,
            "Ticket with this external ID already exists",
        )

    companion object {
        const val MAX_ATTEMPTS = 3
        const val MAX_PAGE_SIZE = 100
    }
}
