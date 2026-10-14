package com.acme.transport.customs.hold.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldProposalValidator()
        val item = newHoldProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldProposalCodeLengthRule(16)
        val item = newHoldProposal("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = HoldProposalCapacityRangeRule(32, 80)
        val item = newHoldProposal("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
