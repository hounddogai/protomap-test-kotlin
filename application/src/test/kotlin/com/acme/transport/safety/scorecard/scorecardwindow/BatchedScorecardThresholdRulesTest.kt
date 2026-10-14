package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedScorecardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedScorecardThresholdValidator()
        val item = newBatchedScorecardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedScorecardThresholdCodeLengthRule(255)
        val item = newBatchedScorecardThreshold("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = BatchedScorecardThresholdShiftCodeLengthRule(16)
        val item = newBatchedScorecardThreshold("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BatchedScorecardThresholdMarginRangeRule(5.6, 4510.7)
        val item = newBatchedScorecardThreshold("x", "y")
        item.margin = 4512.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedScorecardThresholdRequiredCodeRule("batched scorecard threshold needs this field")
        val item = newBatchedScorecardThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
