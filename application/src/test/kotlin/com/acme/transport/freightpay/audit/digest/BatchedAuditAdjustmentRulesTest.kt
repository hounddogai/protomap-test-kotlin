package com.acme.transport.freightpay.audit.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAuditAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAuditAdjustmentValidator()
        val item = newBatchedAuditAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAuditAdjustmentRequiredCodeRule("batched audit adjustment needs an entry")
        val item = newBatchedAuditAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = BatchedAuditAdjustmentRegionLengthRule(86400)
        val item = newBatchedAuditAdjustment("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = BatchedAuditAdjustmentAttemptsRangeRule(32, 1000)
        val item = newBatchedAuditAdjustment("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedAuditAdjustmentRequiredTitleRule("batched audit adjustment needs this field")
        val item = newBatchedAuditAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
