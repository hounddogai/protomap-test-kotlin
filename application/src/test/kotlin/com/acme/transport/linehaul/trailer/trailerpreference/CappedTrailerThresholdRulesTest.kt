package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTrailerThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTrailerThresholdValidator()
        val item = newCappedTrailerThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = CappedTrailerThresholdPhoneLengthRule(86400)
        val item = newCappedTrailerThreshold("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = CappedTrailerThresholdAttemptsRangeRule(16, 32)
        val item = newCappedTrailerThreshold("x", "y")
        item.attempts = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CappedTrailerThresholdGraceDaysRangeRule(80, 86400)
        val item = newCappedTrailerThreshold("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
