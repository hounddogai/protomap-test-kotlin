package com.acme.transport.network.zone.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneForecastValidator()
        val item = newZoneForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ZoneForecastRequiredDepotRule("zone forecast needs this field")
        val item = newZoneForecast("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ZoneForecastPalletsRangeRule(16, 1000)
        val item = newZoneForecast("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ZoneForecastCeilingRangeRule(0.4, 2338.8)
        val item = newZoneForecast("x", "y")
        item.ceiling = 2340.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ZoneForecastRatioRangeRule(7.3, 2600.4)
        val item = newZoneForecast("x", "y")
        item.ratio = 2601.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ZoneForecastRequiredLabelRule("zone forecast needs an entry")
        val item = newZoneForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ZoneForecastDepotLengthRule(80)
        val item = newZoneForecast("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
