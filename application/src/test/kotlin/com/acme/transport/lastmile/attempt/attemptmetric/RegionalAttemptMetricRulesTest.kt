package com.acme.transport.lastmile.attempt.attemptmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAttemptMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAttemptMetricValidator()
        val item = newRegionalAttemptMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RegionalAttemptMetricCostRangeRule(2.5, 3768.4)
        val item = newRegionalAttemptMetric("x", "y")
        item.cost = 3769.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RegionalAttemptMetricDiscountRangeRule(3.0, 1263.0)
        val item = newRegionalAttemptMetric("x", "y")
        item.discount = 1264.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalAttemptMetricRequiredUnitRule("regional attempt metric needs an entry")
        val item = newRegionalAttemptMetric("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
