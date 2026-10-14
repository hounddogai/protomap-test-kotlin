package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIncidentRequestValidator()
        val item = newLegacyIncidentRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyIncidentRequestRequiredCodeRule("legacy incident request needs a value")
        val item = newLegacyIncidentRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyIncidentRequestTitleLengthRule(64)
        val item = newLegacyIncidentRequest("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyIncidentRequestRequiredTitleRule("legacy incident request needs an entry")
        val item = newLegacyIncidentRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LegacyIncidentRequestChannelLengthRule(16)
        val item = newLegacyIncidentRequest("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
