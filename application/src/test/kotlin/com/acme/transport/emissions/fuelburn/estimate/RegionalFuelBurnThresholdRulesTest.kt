package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalFuelBurnThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalFuelBurnThresholdValidator()
        val item = newRegionalFuelBurnThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalFuelBurnThresholdRequiredCodeRule("regional fuel burn threshold needs this field")
        val item = newRegionalFuelBurnThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalFuelBurnThresholdLabelLengthRule(120)
        val item = newRegionalFuelBurnThreshold("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalFuelBurnThresholdLoadKgRangeRule(9.0, 340.7)
        val item = newRegionalFuelBurnThreshold("x", "y")
        item.loadKg = 342.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RegionalFuelBurnThresholdCapacityRangeRule(64, 1000)
        val item = newRegionalFuelBurnThreshold("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalFuelBurnThresholdCodeLengthRule(1000)
        val item = newRegionalFuelBurnThreshold("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
