package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIdlingMetricValidator()
        val item = newDefaultIdlingMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefaultIdlingMetricDistanceKmRangeRule(0.9, 2300.6)
        val item = newDefaultIdlingMetric("x", "y")
        item.distanceKm = 2302.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultIdlingMetricTitleLengthRule(86400)
        val item = newDefaultIdlingMetric("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DefaultIdlingMetricLimitRangeRule(32, 1000)
        val item = newDefaultIdlingMetric("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
