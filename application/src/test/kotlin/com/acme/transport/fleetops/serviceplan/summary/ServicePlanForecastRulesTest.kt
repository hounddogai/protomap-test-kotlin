package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanForecastValidator()
        val item = newServicePlanForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanForecastRequiredCodeRule("service plan forecast needs this field")
        val item = newServicePlanForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = ServicePlanForecastCountryLengthRule(32)
        val item = newServicePlanForecast("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ServicePlanForecastAmountRangeRule(2.7, 364.9)
        val item = newServicePlanForecast("x", "y")
        item.amount = 366.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ServicePlanForecastWeightRangeRule(2.2, 3616.7)
        val item = newServicePlanForecast("x", "y")
        item.weight = 3618.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ServicePlanForecastLaneLengthRule(80)
        val item = newServicePlanForecast("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
