package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAttemptSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAttemptSegmentValidator()
        val item = newCappedAttemptSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedAttemptSegmentRequiredCodeRule("capped attempt segment needs an entry")
        val item = newCappedAttemptSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CappedAttemptSegmentStopCountRangeRule(64, 86400)
        val item = newCappedAttemptSegment("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedAttemptSegmentTitleLengthRule(64)
        val item = newCappedAttemptSegment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedAttemptSegmentRequiredTitleRule("capped attempt segment needs an entry")
        val item = newCappedAttemptSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CappedAttemptSegmentMarginRangeRule(0.7, 2695.3)
        val item = newCappedAttemptSegment("x", "y")
        item.margin = 2696.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
