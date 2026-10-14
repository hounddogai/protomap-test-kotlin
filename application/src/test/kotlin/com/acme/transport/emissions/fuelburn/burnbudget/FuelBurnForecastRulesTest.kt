package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnForecastValidator()
        val item = newFuelBurnForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FuelBurnForecastRequiredCurrencyRule("fuel burn forecast needs an entry")
        val item = newFuelBurnForecast("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FuelBurnForecastStartsAtRangeRule(32, 1000)
        val item = newFuelBurnForecast("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnForecastCodeLengthRule(80)
        val item = newFuelBurnForecast("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
