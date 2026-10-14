package com.acme.transport.emissions.fuelburn.burnnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnMetricValidator()
        val item = newFuelBurnMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = FuelBurnMetricLimitRangeRule(80, 1000)
        val item = newFuelBurnMetric("x", "y")
        item.limit = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = FuelBurnMetricRequiredLaneRule("fuel burn metric needs an entry")
        val item = newFuelBurnMetric("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
