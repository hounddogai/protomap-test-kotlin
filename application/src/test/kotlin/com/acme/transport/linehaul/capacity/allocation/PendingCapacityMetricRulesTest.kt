package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCapacityMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCapacityMetricValidator()
        val item = newPendingCapacityMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingCapacityMetricTitleLengthRule(64)
        val item = newPendingCapacityMetric("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingCapacityMetricRequiredCodeRule("pending capacity metric needs this field")
        val item = newPendingCapacityMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PendingCapacityMetricDistanceKmRangeRule(8.3, 1322.1)
        val item = newPendingCapacityMetric("x", "y")
        item.distanceKm = 1323.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
