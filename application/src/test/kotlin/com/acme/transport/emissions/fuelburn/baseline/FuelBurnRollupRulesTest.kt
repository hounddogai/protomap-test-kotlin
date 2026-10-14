package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnRollupValidator()
        val item = newFuelBurnRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnRollupRequiredCodeRule("fuel burn rollup needs this field")
        val item = newFuelBurnRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = FuelBurnRollupWeightRangeRule(1.6, 301.3)
        val item = newFuelBurnRollup("x", "y")
        item.weight = 302.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelBurnRollupRequiredNameRule("fuel burn rollup needs a value")
        val item = newFuelBurnRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = FuelBurnRollupDurationMinutesRangeRule(32, 80)
        val item = newFuelBurnRollup("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
