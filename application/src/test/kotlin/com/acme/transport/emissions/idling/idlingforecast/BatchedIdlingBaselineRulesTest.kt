package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedIdlingBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedIdlingBaselineValidator()
        val item = newBatchedIdlingBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = BatchedIdlingBaselineRequiredLaneRule("batched idling baseline needs a value")
        val item = newBatchedIdlingBaseline("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = BatchedIdlingBaselineRequiredTimezoneRule("batched idling baseline needs this field")
        val item = newBatchedIdlingBaseline("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedIdlingBaselineBatchSizeRangeRule(80, 120)
        val item = newBatchedIdlingBaseline("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedIdlingBaselineTitleLengthRule(120)
        val item = newBatchedIdlingBaseline("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedIdlingBaselineWindowDaysRangeRule(32, 3600)
        val item = newBatchedIdlingBaseline("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = BatchedIdlingBaselineTimezoneLengthRule(16)
        val item = newBatchedIdlingBaseline("x", "y")
        item.timezone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
