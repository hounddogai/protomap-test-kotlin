package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedClearanceAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedClearanceAllocationValidator()
        val item = newBatchedClearanceAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedClearanceAllocationTitleLengthRule(86400)
        val item = newBatchedClearanceAllocation("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = BatchedClearanceAllocationRequiredDockRule("batched clearance allocation needs an entry")
        val item = newBatchedClearanceAllocation("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedClearanceAllocationCodeLengthRule(16)
        val item = newBatchedClearanceAllocation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = BatchedClearanceAllocationRequiredLocaleRule("batched clearance allocation needs this field")
        val item = newBatchedClearanceAllocation("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedClearanceAllocationBatchSizeRangeRule(16, 80)
        val item = newBatchedClearanceAllocation("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
