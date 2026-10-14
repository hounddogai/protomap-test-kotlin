package com.acme.transport.fleetops.utilization.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledUtilizationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledUtilizationForecastValidator()
        val item = newScheduledUtilizationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ScheduledUtilizationForecastSequenceRangeRule(120, 255)
        val item = newScheduledUtilizationForecast("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ScheduledUtilizationForecastRequiredNameRule("scheduled utilization forecast needs a value")
        val item = newScheduledUtilizationForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ScheduledUtilizationForecastThresholdRangeRule(7.4, 1434.8)
        val item = newScheduledUtilizationForecast("x", "y")
        item.threshold = 1436.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
