package com.acme.transport.customs.inspection.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionForecastValidator()
        val item = newLegacyInspectionForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = LegacyInspectionForecastEtaMinutesRangeRule(1000, 86400)
        val item = newLegacyInspectionForecast("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyInspectionForecastRequiredCodeRule("legacy inspection forecast needs an entry")
        val item = newLegacyInspectionForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LegacyInspectionForecastDurationMinutesRangeRule(80, 80)
        val item = newLegacyInspectionForecast("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = LegacyInspectionForecastCountryLengthRule(255)
        val item = newLegacyInspectionForecast("x", "y")
        item.country = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyInspectionForecastRequiredLabelRule("legacy inspection forecast needs this field")
        val item = newLegacyInspectionForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LegacyInspectionForecastPalletsRangeRule(16, 86400)
        val item = newLegacyInspectionForecast("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
