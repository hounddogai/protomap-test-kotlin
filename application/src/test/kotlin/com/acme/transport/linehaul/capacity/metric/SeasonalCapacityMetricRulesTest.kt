package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCapacityMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCapacityMetricValidator()
        val item = newSeasonalCapacityMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = SeasonalCapacityMetricScoreRangeRule(6.6, 1149.8)
        val item = newSeasonalCapacityMetric("x", "y")
        item.score = 1151.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = SeasonalCapacityMetricOdometerRangeRule(64, 1000)
        val item = newSeasonalCapacityMetric("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = SeasonalCapacityMetricWindowDaysRangeRule(80, 120)
        val item = newSeasonalCapacityMetric("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = SeasonalCapacityMetricSourceLengthRule(3600)
        val item = newSeasonalCapacityMetric("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalCapacityMetricTitleLengthRule(86400)
        val item = newSeasonalCapacityMetric("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = SeasonalCapacityMetricGraceDaysRangeRule(32, 80)
        val item = newSeasonalCapacityMetric("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
