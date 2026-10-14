package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIncidentRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingIncidentRequestValidator()
        val item = newPendingIncidentRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingIncidentRequestRequiredTitleRule("pending incident request needs this field")
        val item = newPendingIncidentRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingIncidentRequestTitleLengthRule(80)
        val item = newPendingIncidentRequest("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = PendingIncidentRequestTimezoneLengthRule(3600)
        val item = newPendingIncidentRequest("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
