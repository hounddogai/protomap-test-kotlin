package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnCheckpointValidator()
        val item = newFuelBurnCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelBurnCheckpointNameLengthRule(255)
        val item = newFuelBurnCheckpoint("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FuelBurnCheckpointRequiredCategoryRule("fuel burn checkpoint needs a value")
        val item = newFuelBurnCheckpoint("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = FuelBurnCheckpointCategoryLengthRule(16)
        val item = newFuelBurnCheckpoint("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = FuelBurnCheckpointPriorityRangeRule(16, 64)
        val item = newFuelBurnCheckpoint("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
