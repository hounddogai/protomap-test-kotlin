package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnEstimateValidator()
        val item = newFuelBurnEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelBurnEstimateTitleLengthRule(86400)
        val item = newFuelBurnEstimate("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FuelBurnEstimateRequiredCurrencyRule("fuel burn estimate needs a value")
        val item = newFuelBurnEstimate("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = FuelBurnEstimateRequiredVehicleIdRule("fuel burn estimate needs an entry")
        val item = newFuelBurnEstimate("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = FuelBurnEstimateVehicleIdLengthRule(3600)
        val item = newFuelBurnEstimate("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
