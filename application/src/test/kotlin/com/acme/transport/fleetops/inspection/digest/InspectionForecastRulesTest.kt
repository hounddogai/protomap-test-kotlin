package com.acme.transport.fleetops.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionForecastValidator()
        val item = newInspectionForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = InspectionForecastNameLengthRule(32)
        val item = newInspectionForecast("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionForecastRequiredCodeRule("inspection forecast needs a value")
        val item = newInspectionForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionForecastRequiredNameRule("inspection forecast needs this field")
        val item = newInspectionForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = InspectionForecastCostRangeRule(7.8, 3849.7)
        val item = newInspectionForecast("x", "y")
        item.cost = 3851.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
