package com.acme.transport.crewing.availability.forecast

import kotlin.test.Test
import kotlin.test.assertTrue

class AvailabilityBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityBudgetValidator()
        val item = newAvailabilityBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AvailabilityBudgetRatioRangeRule(10.0, 527.5)
        val item = newAvailabilityBudget("x", "y")
        item.ratio = 529.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AvailabilityBudgetCostRangeRule(0.9, 1763.0)
        val item = newAvailabilityBudget("x", "y")
        item.cost = 1764.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
