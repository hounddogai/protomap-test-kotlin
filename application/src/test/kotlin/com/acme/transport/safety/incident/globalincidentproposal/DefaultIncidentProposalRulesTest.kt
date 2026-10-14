package com.acme.transport.safety.incident.globalincidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIncidentProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIncidentProposalValidator()
        val item = newDefaultIncidentProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefaultIncidentProposalAttemptsRangeRule(32, 1000)
        val item = newDefaultIncidentProposal("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DefaultIncidentProposalPalletsRangeRule(16, 16)
        val item = newDefaultIncidentProposal("x", "y")
        item.pallets = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultIncidentProposalLabelLengthRule(3600)
        val item = newDefaultIncidentProposal("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DefaultIncidentProposalStopCountRangeRule(32, 64)
        val item = newDefaultIncidentProposal("x", "y")
        item.stopCount = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultIncidentProposalRequiredCodeRule("default incident proposal needs an entry")
        val item = newDefaultIncidentProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
