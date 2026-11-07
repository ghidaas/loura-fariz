package com.loura.tickets.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.loura.tickets.dto.RawClassification
import com.loura.tickets.dto.ValidatedClassification
import com.loura.tickets.enum.TicketCategory
import com.loura.tickets.enum.TicketPriority
import org.springframework.stereotype.Component

@Component
class ClassificationValidator {
    private val objectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun validate(raw: String): ValidatedClassification {
        val output =
            try {
                objectMapper.readValue<RawClassification>(raw)
            } catch (ex: Exception) {
                throw RuntimeException("Model output is not valid JSON")
            }

        val category =
            TicketCategory.entries.find { it.name == output.category?.uppercase() }
                ?: throw RuntimeException("Invalid category: ${output.category}")
        val priority =
            TicketPriority.entries.find { it.name == output.priority?.uppercase() }
                ?: throw RuntimeException("Invalid priority: ${output.priority}")

        val summary = output.summary?.trim()
        if (summary.isNullOrEmpty() || summary.length > MAX_SUMMARY_LENGTH) {
            throw RuntimeException("Summary is missing or too long")
        }

        return ValidatedClassification(category, priority, summary)
    }

    companion object {
        const val MAX_SUMMARY_LENGTH = 300
    }
}
