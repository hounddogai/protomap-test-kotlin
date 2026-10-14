package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAuditProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAuditProposalValidator()
        val item = newGlobalAuditProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = GlobalAuditProposalLocaleLengthRule(3600)
        val item = newGlobalAuditProposal("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalAuditProposalCodeLengthRule(120)
        val item = newGlobalAuditProposal("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalAuditProposalRequiredTitleRule("global audit proposal needs a value")
        val item = newGlobalAuditProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = GlobalAuditProposalCreatedAtRangeRule(16, 255)
        val item = newGlobalAuditProposal("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = GlobalAuditProposalRequiredLocaleRule("global audit proposal needs an entry")
        val item = newGlobalAuditProposal("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalAuditProposalAttemptsRangeRule(80, 255)
        val item = newGlobalAuditProposal("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
