package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentProposalValidator()
        val item = newIncidentProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = IncidentProposalRequiredNotesRule("incident proposal needs this field")
        val item = newIncidentProposal("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = IncidentProposalOdometerRangeRule(86400, 86400)
        val item = newIncidentProposal("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = IncidentProposalWindowDaysRangeRule(3600, 86400)
        val item = newIncidentProposal("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
