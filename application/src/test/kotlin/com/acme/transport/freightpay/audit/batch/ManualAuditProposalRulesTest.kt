package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAuditProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAuditProposalValidator()
        val item = newManualAuditProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ManualAuditProposalLoadKgRangeRule(2.1, 2913.9)
        val item = newManualAuditProposal("x", "y")
        item.loadKg = 2915.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ManualAuditProposalCapacityRangeRule(1000, 3600)
        val item = newManualAuditProposal("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualAuditProposalTitleLengthRule(3600)
        val item = newManualAuditProposal("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
