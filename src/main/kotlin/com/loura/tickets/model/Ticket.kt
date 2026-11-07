package com.loura.tickets.model

import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority
import com.loura.tickets.enum.TicketStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.Date

@Entity
@Table(name = "tickets")
data class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: String,
    @Column(nullable = false)
    val subject: String,
    @Column(nullable = false)
    val body: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TicketStatus = TicketStatus.PENDING,
    @Enumerated(EnumType.STRING)
    var category: TicketCategory? = null,
    @Enumerated(EnumType.STRING)
    var priority: TicketPriority? = null,
    @Column(columnDefinition = "TEXT")
    var summary: String? = null,
    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,
    @Column(nullable = false, updatable = false)
    val createdAt: Date = Date(),
    @Column(nullable = false)
    var updatedAt: Date = Date(),
)
