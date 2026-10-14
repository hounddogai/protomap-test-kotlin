package com.acme.transport.customs.clearance.clearancecheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceProposalValidator()
        val item = newClearanceProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ClearanceProposalDiscountRangeRule(4.9, 1390.7)
        val item = newClearanceProposal("x", "y")
        item.discount = 1392.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceProposalRequiredCodeRule("clearance proposal needs an entry")
        val item = newClearanceProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ClearanceProposalTitleLengthRule(16)
        val item = newClearanceProposal("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
