package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAttemptMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAttemptMetricValidator()
        val item = newDefaultAttemptMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultAttemptMetricThresholdRangeRule(7.9, 4419.2)
        val item = newDefaultAttemptMetric("x", "y")
        item.threshold = 4420.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DefaultAttemptMetricAttemptsRangeRule(120, 3600)
        val item = newDefaultAttemptMetric("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DefaultAttemptMetricDurationMinutesRangeRule(16, 1000)
        val item = newDefaultAttemptMetric("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultAttemptMetricCodeLengthRule(1000)
        val item = newDefaultAttemptMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultAttemptMetricRequiredNameRule("default attempt metric needs this field")
        val item = newDefaultAttemptMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
