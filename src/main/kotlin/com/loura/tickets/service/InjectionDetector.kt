package com.loura.tickets.service

import org.springframework.stereotype.Service

/**
 * Checks whether a ticket body looks like a prompt-injection attempt.
 *
 * Fake implementation: matches a few well-known phrases. In production this would call a dedicated
 * classifier (e.g. Llama Prompt Guard or Azure Prompt Shields). It is easy to bypass by rewording,
 * so it is an extra signal only, not a replacement for validating the model output.
 */
@Service
class InjectionDetector {
    fun isSafe(body: String): Boolean {
        val text = body.lowercase()
        return SUSPICIOUS_PATTERNS.none { it.containsMatchIn(text) }
    }

    companion object {
        private val SUSPICIOUS_PATTERNS =
            listOf(
                Regex("""ignore (all )?(the )?(previous|prior|above) instructions"""),
                Regex("""disregard (all )?(the )?(previous|prior|above)"""),
                Regex("""system prompt"""),
                Regex("""you are now"""),
                Regex("""</?\s*(system|ticket|instructions?)\s*>"""),
            )
    }
}
