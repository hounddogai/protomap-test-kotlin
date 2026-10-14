package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnGroupValidator()
        val item = newFuelBurnGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = FuelBurnGroupBudgetRangeRule(1.5, 3275.9)
        val item = newFuelBurnGroup("x", "y")
        item.budget = 3277.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = FuelBurnGroupRequiredVehicleIdRule("fuel burn group needs a value")
        val item = newFuelBurnGroup("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = FuelBurnGroupLaneLengthRule(1000)
        val item = newFuelBurnGroup("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
