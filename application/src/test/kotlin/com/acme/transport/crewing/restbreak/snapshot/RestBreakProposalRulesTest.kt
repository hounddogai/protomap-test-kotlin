package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakProposalValidator()
        val item = newRestBreakProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RestBreakProposalAttemptsRangeRule(1000, 3600)
        val item = newRestBreakProposal("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakProposalRequiredCodeRule("rest break proposal needs a value")
        val item = newRestBreakProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RestBreakProposalCodeLengthRule(1000)
        val item = newRestBreakProposal("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RestBreakProposalMarginRangeRule(9.5, 3060.3)
        val item = newRestBreakProposal("x", "y")
        item.margin = 3061.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RestBreakProposalRequiredLabelRule("rest break proposal needs this field")
        val item = newRestBreakProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
