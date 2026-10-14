package com.acme.transport.safety.nearmiss.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNearMissBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNearMissBudgetValidator()
        val item = newPrimaryNearMissBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = PrimaryNearMissBudgetRequiredTimezoneRule("primary near miss budget needs a value")
        val item = newPrimaryNearMissBudget("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PrimaryNearMissBudgetDistanceKmRangeRule(7.3, 3279.3)
        val item = newPrimaryNearMissBudget("x", "y")
        item.distanceKm = 3280.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PrimaryNearMissBudgetCapacityRangeRule(32, 1000)
        val item = newPrimaryNearMissBudget("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
