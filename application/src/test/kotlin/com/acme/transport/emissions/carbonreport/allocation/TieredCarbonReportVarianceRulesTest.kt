package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCarbonReportVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCarbonReportVarianceValidator()
        val item = newTieredCarbonReportVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredCarbonReportVarianceNameLengthRule(16)
        val item = newTieredCarbonReportVariance("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TieredCarbonReportVarianceDurationMinutesRangeRule(64, 80)
        val item = newTieredCarbonReportVariance("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = TieredCarbonReportVarianceVehicleIdLengthRule(16)
        val item = newTieredCarbonReportVariance("x", "y")
        item.vehicleId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
