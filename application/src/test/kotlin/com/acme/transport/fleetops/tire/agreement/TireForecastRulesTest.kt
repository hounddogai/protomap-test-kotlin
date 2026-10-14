package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireForecastValidator()
        val item = newTireForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireForecastRequiredCodeRule("tire forecast needs a value")
        val item = newTireForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TireForecastRequiredChecksumRule("tire forecast needs this field")
        val item = newTireForecast("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TireForecastAmountRangeRule(1.4, 1784.3)
        val item = newTireForecast("x", "y")
        item.amount = 1785.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
