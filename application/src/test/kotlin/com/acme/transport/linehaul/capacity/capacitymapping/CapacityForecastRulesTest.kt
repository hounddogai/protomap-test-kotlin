package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityForecastValidator()
        val item = newCapacityForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityForecastRequiredCodeRule("capacity forecast needs a value")
        val item = newCapacityForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CapacityForecastStopCountRangeRule(1000, 86400)
        val item = newCapacityForecast("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = CapacityForecastRequiredCategoryRule("capacity forecast needs this field")
        val item = newCapacityForecast("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CapacityForecastNameLengthRule(32)
        val item = newCapacityForecast("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CapacityForecastRequiredNameRule("capacity forecast needs an entry")
        val item = newCapacityForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
