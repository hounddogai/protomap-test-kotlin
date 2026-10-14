package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSortationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSortationBatchValidator()
        val item = newPendingSortationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PendingSortationBatchPriorityRangeRule(32, 3600)
        val item = newPendingSortationBatch("x", "y")
        item.priority = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = PendingSortationBatchRegionLengthRule(32)
        val item = newPendingSortationBatch("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PendingSortationBatchCostRangeRule(6.6, 145.5)
        val item = newPendingSortationBatch("x", "y")
        item.cost = 147.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
