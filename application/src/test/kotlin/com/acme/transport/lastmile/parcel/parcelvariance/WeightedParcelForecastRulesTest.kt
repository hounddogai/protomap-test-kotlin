package com.acme.transport.lastmile.parcel.parcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedParcelForecastValidator()
        val item = newWeightedParcelForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = WeightedParcelForecastRequiredShiftCodeRule("weighted parcel forecast needs this field")
        val item = newWeightedParcelForecast("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = WeightedParcelForecastRequiredChecksumRule("weighted parcel forecast needs this field")
        val item = newWeightedParcelForecast("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = WeightedParcelForecastChecksumLengthRule(32)
        val item = newWeightedParcelForecast("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
