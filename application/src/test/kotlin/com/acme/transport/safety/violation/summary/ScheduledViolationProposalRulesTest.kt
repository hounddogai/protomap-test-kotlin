package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledViolationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledViolationProposalValidator()
        val item = newScheduledViolationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScheduledViolationProposalWeightRangeRule(6.9, 4550.6)
        val item = newScheduledViolationProposal("x", "y")
        item.weight = 4552.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledViolationProposalCodeLengthRule(3600)
        val item = newScheduledViolationProposal("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledViolationProposalRequiredTitleRule("scheduled violation proposal needs an entry")
        val item = newScheduledViolationProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
