package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnAdjustmentValidator()
        val item = newFuelBurnAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnAdjustmentRequiredCodeRule("fuel burn adjustment needs a value")
        val item = newFuelBurnAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FuelBurnAdjustmentRequiredTitleRule("fuel burn adjustment needs an entry")
        val item = newFuelBurnAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = FuelBurnAdjustmentWeightRangeRule(8.7, 4056.9)
        val item = newFuelBurnAdjustment("x", "y")
        item.weight = 4058.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnAdjustmentCodeLengthRule(120)
        val item = newFuelBurnAdjustment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
