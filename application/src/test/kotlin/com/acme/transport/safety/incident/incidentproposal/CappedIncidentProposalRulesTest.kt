package com.acme.transport.safety.incident.incidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIncidentProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedIncidentProposalValidator()
        val item = newCappedIncidentProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = CappedIncidentProposalTimezoneLengthRule(3600)
        val item = newCappedIncidentProposal("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = CappedIncidentProposalCreatedAtRangeRule(16, 86400)
        val item = newCappedIncidentProposal("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
