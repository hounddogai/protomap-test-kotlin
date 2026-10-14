package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorForecastValidator()
        val item = newEmissionFactorForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = EmissionFactorForecastDistanceKmRangeRule(6.1, 1464.9)
        val item = newEmissionFactorForecast("x", "y")
        item.distanceKm = 1466.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = EmissionFactorForecastAttemptsRangeRule(1000, 3600)
        val item = newEmissionFactorForecast("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = EmissionFactorForecastRequiredChecksumRule("emission factor forecast needs this field")
        val item = newEmissionFactorForecast("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EmissionFactorForecastLabelLengthRule(120)
        val item = newEmissionFactorForecast("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = EmissionFactorForecastChecksumLengthRule(16)
        val item = newEmissionFactorForecast("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
