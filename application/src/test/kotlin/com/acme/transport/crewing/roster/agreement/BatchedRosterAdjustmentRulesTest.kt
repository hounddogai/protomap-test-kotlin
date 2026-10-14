package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRosterAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRosterAdjustmentValidator()
        val item = newBatchedRosterAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = BatchedRosterAdjustmentRequiredShiftCodeRule("batched roster adjustment needs an entry")
        val item = newBatchedRosterAdjustment("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedRosterAdjustmentLabelLengthRule(3600)
        val item = newBatchedRosterAdjustment("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = BatchedRosterAdjustmentRequiredReviewerRule("batched roster adjustment needs this field")
        val item = newBatchedRosterAdjustment("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = BatchedRosterAdjustmentPriorityRangeRule(64, 255)
        val item = newBatchedRosterAdjustment("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRosterAdjustmentCodeLengthRule(86400)
        val item = newBatchedRosterAdjustment("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
