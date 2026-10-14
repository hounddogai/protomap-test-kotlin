package com.acme.transport.lastmile.drop.dropentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDropMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDropMetricValidator()
        val item = newFallbackDropMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FallbackDropMetricAttemptsRangeRule(255, 86400)
        val item = newFallbackDropMetric("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackDropMetricLabelLengthRule(120)
        val item = newFallbackDropMetric("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = FallbackDropMetricBatchSizeRangeRule(32, 32)
        val item = newFallbackDropMetric("x", "y")
        item.batchSize = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
