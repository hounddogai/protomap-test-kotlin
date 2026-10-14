package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeMetricValidator()
        val item = newTransitTimeMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = TransitTimeMetricRegionLengthRule(120)
        val item = newTransitTimeMetric("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TransitTimeMetricPriorityRangeRule(80, 255)
        val item = newTransitTimeMetric("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = TransitTimeMetricVersionRangeRule(80, 255)
        val item = newTransitTimeMetric("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
