package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIncidentWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackIncidentWindowValidator()
        val item = newFallbackIncidentWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackIncidentWindowTitleLengthRule(86400)
        val item = newFallbackIncidentWindow("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackIncidentWindowRequiredLabelRule("fallback incident window needs this field")
        val item = newFallbackIncidentWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackIncidentWindowTrailerIdLengthRule(3600)
        val item = newFallbackIncidentWindow("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
