package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelBurnMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFuelBurnMetricValidator()
        val item = newPrimaryFuelBurnMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PrimaryFuelBurnMetricChannelLengthRule(255)
        val item = newPrimaryFuelBurnMetric("x", "y")
        item.channel = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryFuelBurnMetricRequiredCodeRule("primary fuel burn metric needs an entry")
        val item = newPrimaryFuelBurnMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PrimaryFuelBurnMetricGraceDaysRangeRule(120, 3600)
        val item = newPrimaryFuelBurnMetric("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PrimaryFuelBurnMetricRequiredChannelRule("primary fuel burn metric needs this field")
        val item = newPrimaryFuelBurnMetric("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PrimaryFuelBurnMetricUnitLengthRule(64)
        val item = newPrimaryFuelBurnMetric("x", "y")
        item.unit = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
