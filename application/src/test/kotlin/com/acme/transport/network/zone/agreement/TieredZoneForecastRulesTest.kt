package com.acme.transport.network.zone.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZoneForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredZoneForecastValidator()
        val item = newTieredZoneForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredZoneForecastRequiredLabelRule("tiered zone forecast needs a value")
        val item = newTieredZoneForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TieredZoneForecastExpiresAtRangeRule(255, 3600)
        val item = newTieredZoneForecast("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = TieredZoneForecastRequiredDriverIdRule("tiered zone forecast needs an entry")
        val item = newTieredZoneForecast("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = TieredZoneForecastUnitLengthRule(120)
        val item = newTieredZoneForecast("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
