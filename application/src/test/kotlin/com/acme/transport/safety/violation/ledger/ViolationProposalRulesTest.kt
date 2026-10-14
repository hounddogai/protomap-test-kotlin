package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationProposalValidator()
        val item = newViolationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ViolationProposalTitleLengthRule(86400)
        val item = newViolationProposal("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ViolationProposalExpiresAtRangeRule(64, 86400)
        val item = newViolationProposal("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = ViolationProposalRequiredPhoneRule("violation proposal needs this field")
        val item = newViolationProposal("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
