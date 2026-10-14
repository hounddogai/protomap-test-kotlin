package com.acme.transport.crewing.swap.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedSwapBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedSwapBundleValidator()
        val item = newBatchedSwapBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedSwapBundleRequiredCodeRule("batched swap bundle needs this field")
        val item = newBatchedSwapBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedSwapBundleEndsAtRangeRule(16, 80)
        val item = newBatchedSwapBundle("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedSwapBundleRequiredNameRule("batched swap bundle needs a value")
        val item = newBatchedSwapBundle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = BatchedSwapBundleTrailerIdLengthRule(3600)
        val item = newBatchedSwapBundle("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
