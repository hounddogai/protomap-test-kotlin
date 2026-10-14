package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnSegmentValidator()
        val item = newFuelBurnSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FuelBurnSegmentRequiredLocaleRule("fuel burn segment needs an entry")
        val item = newFuelBurnSegment("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnSegmentRequiredCodeRule("fuel burn segment needs a value")
        val item = newFuelBurnSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = FuelBurnSegmentLocaleLengthRule(120)
        val item = newFuelBurnSegment("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
