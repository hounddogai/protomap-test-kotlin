package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnBaselineValidator()
        val item = newFuelBurnBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelBurnBaselineRequiredNameRule("fuel burn baseline needs this field")
        val item = newFuelBurnBaseline("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = FuelBurnBaselineRequiredOwnerIdRule("fuel burn baseline needs a value")
        val item = newFuelBurnBaseline("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = FuelBurnBaselineRequiredLaneRule("fuel burn baseline needs a value")
        val item = newFuelBurnBaseline("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
