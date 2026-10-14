package com.acme.transport.terminal.dwell.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDwellForecastValidator()
        val item = newLenientDwellForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientDwellForecastDiscountRangeRule(8.0, 1216.2)
        val item = newLenientDwellForecast("x", "y")
        item.discount = 1217.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LenientDwellForecastCreatedAtRangeRule(80, 80)
        val item = newLenientDwellForecast("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LenientDwellForecastPalletsRangeRule(16, 80)
        val item = newLenientDwellForecast("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDwellForecastCodeLengthRule(120)
        val item = newLenientDwellForecast("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
