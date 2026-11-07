package com.loura.tickets.controller

import com.loura.tickets.dto.CreateTicketRequest
import com.loura.tickets.dto.TicketResponse
import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority
import com.loura.tickets.service.TicketService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketService: TicketService,
) {
    @PostMapping
    fun createTicket(
        @Valid @RequestBody request: CreateTicketRequest,
    ): ResponseEntity<TicketResponse> = ResponseEntity.ok(ticketService.createTicket(request))

    @GetMapping("/{id}")
    fun getTicketById(
        @PathVariable id: String,
    ): ResponseEntity<TicketResponse> = ResponseEntity.ok(ticketService.getTicketByExternalId(id))

    @GetMapping
    fun getTickets(
        @RequestParam(required = false) category: TicketCategory?,
        @RequestParam(required = false) priority: TicketPriority?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Page<TicketResponse>> =
        ResponseEntity.ok(
            ticketService.getTickets(
                category,
                priority,
                page,
                size,
            ),
        )
}
