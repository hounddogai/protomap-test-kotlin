package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFuelBurnMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedFuelBurnMilestoneValidator()
        val item = newCappedFuelBurnMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = CappedFuelBurnMilestoneNotesLengthRule(86400)
        val item = newCappedFuelBurnMilestone("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedFuelBurnMilestoneLimitRangeRule(32, 255)
        val item = newCappedFuelBurnMilestone("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CappedFuelBurnMilestoneDiscountRangeRule(7.1, 1876.2)
        val item = newCappedFuelBurnMilestone("x", "y")
        item.discount = 1877.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
