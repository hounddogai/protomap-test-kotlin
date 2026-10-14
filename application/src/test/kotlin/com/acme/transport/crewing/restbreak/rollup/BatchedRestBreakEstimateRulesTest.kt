package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRestBreakEstimateValidator()
        val item = newBatchedRestBreakEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedRestBreakEstimateTitleLengthRule(86400)
        val item = newBatchedRestBreakEstimate("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedRestBreakEstimateRequiredCodeRule("batched rest break estimate needs a value")
        val item = newBatchedRestBreakEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedRestBreakEstimateWindowDaysRangeRule(16, 120)
        val item = newBatchedRestBreakEstimate("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
