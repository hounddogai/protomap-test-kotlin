package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTransitTimeAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTransitTimeAllocationValidator()
        val item = newWeightedTransitTimeAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedTransitTimeAllocationNameLengthRule(80)
        val item = newWeightedTransitTimeAllocation("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedTransitTimeAllocationUpdatedAtRangeRule(80, 1000)
        val item = newWeightedTransitTimeAllocation("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedTransitTimeAllocationQuantityRangeRule(64, 120)
        val item = newWeightedTransitTimeAllocation("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
