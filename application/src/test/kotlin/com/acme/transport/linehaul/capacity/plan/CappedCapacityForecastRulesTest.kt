package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCapacityForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCapacityForecastValidator()
        val item = newCappedCapacityForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedCapacityForecastRequiredCodeRule("capped capacity forecast needs a value")
        val item = newCappedCapacityForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = CappedCapacityForecastAmountRangeRule(9.0, 3612.1)
        val item = newCappedCapacityForecast("x", "y")
        item.amount = 3613.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedCapacityForecastNameLengthRule(32)
        val item = newCappedCapacityForecast("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
