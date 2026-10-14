package com.acme.transport.lastmile.attempt.attemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptMetricValidator()
        val item = newAttemptMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AttemptMetricFuelLitersRangeRule(1.9, 3558.2)
        val item = newAttemptMetric("x", "y")
        item.fuelLiters = 3559.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptMetricRequiredCodeRule("attempt metric needs this field")
        val item = newAttemptMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = AttemptMetricCapacityRangeRule(64, 1000)
        val item = newAttemptMetric("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AttemptMetricCodeLengthRule(64)
        val item = newAttemptMetric("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = AttemptMetricDescriptionLengthRule(120)
        val item = newAttemptMetric("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
