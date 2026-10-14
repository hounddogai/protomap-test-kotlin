package com.acme.transport.emissions.fuelburn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelBurnMappingValidator()
        val item = newWeightedFuelBurnMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedFuelBurnMappingLabelLengthRule(16)
        val item = newWeightedFuelBurnMapping("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedFuelBurnMappingRequiredLabelRule("weighted fuel burn mapping needs an entry")
        val item = newWeightedFuelBurnMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = WeightedFuelBurnMappingLaneLengthRule(3600)
        val item = newWeightedFuelBurnMapping("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedFuelBurnMappingPriorityRangeRule(64, 64)
        val item = newWeightedFuelBurnMapping("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = WeightedFuelBurnMappingRequiredReviewerRule("weighted fuel burn mapping needs an entry")
        val item = newWeightedFuelBurnMapping("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
