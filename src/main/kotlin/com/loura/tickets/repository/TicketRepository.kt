package com.loura.tickets.repository

import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority
import com.loura.tickets.enum.TicketStatus
import com.loura.tickets.model.Ticket
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    @Query(
        """
        SELECT t FROM Ticket t
        WHERE (:category IS NULL OR t.category = :category)
        AND (:priority IS NULL OR t.priority = :priority)
    """,
    )
    fun findTickets(
        @Param("category") category: TicketCategory?,
        @Param("priority") priority: TicketPriority?,
        pageable: Pageable,
    ): Page<Ticket>

    fun findByExternalId(externalId: String): Ticket?

    @Query(
        """
    SELECT t
    FROM Ticket t
    WHERE t.status = :status
""",
    )
    fun findPendingTickets(
        @Param("status") status: TicketStatus = TicketStatus.PENDING,
    ): List<Ticket>

    @Modifying
    @Query(
        """
    UPDATE Ticket t
    SET t.priority = :priority,
        t.category = :category,
        t.summary = :summary,
        t.status = :status,
        t.updatedAt = CURRENT_TIMESTAMP
    WHERE t.id = :id
""",
    )
    fun updateClassification(
        id: Long,
        priority: TicketPriority,
        category: TicketCategory,
        summary: String,
        status: TicketStatus,
    ): Int
}
