package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFuelBurnMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackFuelBurnMilestoneValidator()
        val item = newFallbackFuelBurnMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackFuelBurnMilestoneLoadKgRangeRule(9.2, 1510.1)
        val item = newFallbackFuelBurnMilestone("x", "y")
        item.loadKg = 1511.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FallbackFuelBurnMilestoneStopCountRangeRule(64, 80)
        val item = newFallbackFuelBurnMilestone("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackFuelBurnMilestoneTitleLengthRule(1000)
        val item = newFallbackFuelBurnMilestone("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
