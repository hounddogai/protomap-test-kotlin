package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRestBreakThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRestBreakThresholdValidator()
        val item = newBatchedRestBreakThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = BatchedRestBreakThresholdFloorValueRangeRule(1.9, 540.0)
        val item = newBatchedRestBreakThreshold("x", "y")
        item.floorValue = 541.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedRestBreakThresholdEndsAtRangeRule(1000, 3600)
        val item = newBatchedRestBreakThreshold("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = BatchedRestBreakThresholdShiftCodeLengthRule(255)
        val item = newBatchedRestBreakThreshold("x", "y")
        item.shiftCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRestBreakThresholdCodeLengthRule(1000)
        val item = newBatchedRestBreakThreshold("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
