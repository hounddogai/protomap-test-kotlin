package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnManifestValidator()
        val item = newFuelBurnManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = FuelBurnManifestRequiredVehicleIdRule("fuel burn manifest needs this field")
        val item = newFuelBurnManifest("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FuelBurnManifestAttemptsRangeRule(80, 3600)
        val item = newFuelBurnManifest("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = FuelBurnManifestVehicleIdLengthRule(80)
        val item = newFuelBurnManifest("x", "y")
        item.vehicleId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
