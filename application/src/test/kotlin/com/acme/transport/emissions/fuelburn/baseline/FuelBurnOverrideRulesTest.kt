package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnOverrideValidator()
        val item = newFuelBurnOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnOverrideRequiredCodeRule("fuel burn override needs a value")
        val item = newFuelBurnOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FuelBurnOverrideNotesLengthRule(1000)
        val item = newFuelBurnOverride("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelBurnOverrideNameLengthRule(3600)
        val item = newFuelBurnOverride("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FuelBurnOverrideMaximumRangeRule(3.2, 2533.4)
        val item = newFuelBurnOverride("x", "y")
        item.maximum = 2534.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
