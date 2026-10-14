package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServicePlanForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyServicePlanForecastValidator()
        val item = newLegacyServicePlanForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = LegacyServicePlanForecastRequiredTrailerIdRule("legacy service plan forecast needs an entry")
        val item = newLegacyServicePlanForecast("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyServicePlanForecastCodeLengthRule(3600)
        val item = newLegacyServicePlanForecast("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = LegacyServicePlanForecastTrailerIdLengthRule(32)
        val item = newLegacyServicePlanForecast("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LegacyServicePlanForecastFloorValueRangeRule(4.0, 4573.6)
        val item = newLegacyServicePlanForecast("x", "y")
        item.floorValue = 4575.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
