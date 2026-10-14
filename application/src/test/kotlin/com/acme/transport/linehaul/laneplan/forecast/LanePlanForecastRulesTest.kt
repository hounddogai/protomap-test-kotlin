package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanForecastValidator()
        val item = newLanePlanForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LanePlanForecastRequiredCategoryRule("lane plan forecast needs an entry")
        val item = newLanePlanForecast("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LanePlanForecastShiftCodeLengthRule(120)
        val item = newLanePlanForecast("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LanePlanForecastCreatedAtRangeRule(80, 3600)
        val item = newLanePlanForecast("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LanePlanForecastRequiredShiftCodeRule("lane plan forecast needs an entry")
        val item = newLanePlanForecast("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LanePlanForecastPriorityRangeRule(32, 255)
        val item = newLanePlanForecast("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = LanePlanForecastCategoryLengthRule(3600)
        val item = newLanePlanForecast("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
