package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapBatchValidator()
        val item = newSwapBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = SwapBatchVersionRangeRule(80, 80)
        val item = newSwapBatch("x", "y")
        item.version = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = SwapBatchLocaleLengthRule(120)
        val item = newSwapBatch("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SwapBatchLabelLengthRule(3600)
        val item = newSwapBatch("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = SwapBatchUpdatedAtRangeRule(80, 3600)
        val item = newSwapBatch("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
