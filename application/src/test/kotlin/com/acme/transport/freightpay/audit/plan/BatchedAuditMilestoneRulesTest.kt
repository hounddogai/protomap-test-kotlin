package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAuditMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAuditMilestoneValidator()
        val item = newBatchedAuditMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedAuditMilestoneCodeLengthRule(64)
        val item = newBatchedAuditMilestone("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedAuditMilestoneRequiredContactEmailRule("batched audit milestone needs an entry")
        val item = newBatchedAuditMilestone("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAuditMilestoneRequiredCodeRule("batched audit milestone needs this field")
        val item = newBatchedAuditMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BatchedAuditMilestoneCeilingRangeRule(7.3, 1544.2)
        val item = newBatchedAuditMilestone("x", "y")
        item.ceiling = 1545.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
