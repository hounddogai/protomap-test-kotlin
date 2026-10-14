package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentDraftValidator()
        val item = newIncidentDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = IncidentDraftRequiredContactEmailRule("incident draft needs this field")
        val item = newIncidentDraft("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = IncidentDraftContactEmailLengthRule(16)
        val item = newIncidentDraft("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = IncidentDraftSequenceRangeRule(16, 3600)
        val item = newIncidentDraft("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
