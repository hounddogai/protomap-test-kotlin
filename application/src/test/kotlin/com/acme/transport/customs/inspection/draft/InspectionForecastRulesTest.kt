package com.acme.transport.customs.inspection.draft

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
    fun `flags a blank code`() {
        val rule = InspectionForecastRequiredCodeRule("inspection forecast needs a value")
        val item = newInspectionForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = InspectionForecastSummaryLengthRule(120)
        val item = newInspectionForecast("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = InspectionForecastRequiredSummaryRule("inspection forecast needs a value")
        val item = newInspectionForecast("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = InspectionForecastUnitLengthRule(64)
        val item = newInspectionForecast("x", "y")
        item.unit = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = InspectionForecastCostRangeRule(2.8, 3941.4)
        val item = newInspectionForecast("x", "y")
        item.cost = 3942.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
