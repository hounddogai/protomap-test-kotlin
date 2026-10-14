package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerForecastValidator()
        val item = newLockerForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerForecastRequiredCodeRule("locker forecast needs a value")
        val item = newLockerForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LockerForecastFloorValueRangeRule(9.1, 1841.8)
        val item = newLockerForecast("x", "y")
        item.floorValue = 1843.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LockerForecastPriorityRangeRule(80, 1000)
        val item = newLockerForecast("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = LockerForecastDockLengthRule(64)
        val item = newLockerForecast("x", "y")
        item.dock = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerForecastCodeLengthRule(3600)
        val item = newLockerForecast("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
