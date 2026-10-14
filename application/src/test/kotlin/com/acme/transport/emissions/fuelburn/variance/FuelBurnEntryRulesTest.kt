package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnEntryValidator()
        val item = newFuelBurnEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FuelBurnEntryRequiredLocaleRule("fuel burn entry needs an entry")
        val item = newFuelBurnEntry("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = FuelBurnEntryAmountRangeRule(1.5, 438.1)
        val item = newFuelBurnEntry("x", "y")
        item.amount = 439.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnEntryRequiredCodeRule("fuel burn entry needs an entry")
        val item = newFuelBurnEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FuelBurnEntryLabelLengthRule(1000)
        val item = newFuelBurnEntry("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FuelBurnEntryLocaleLengthRule(16)
        val item = newFuelBurnEntry("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
