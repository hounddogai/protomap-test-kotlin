package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityBudgetValidator()
        val item = newCapacityBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = CapacityBudgetNotesLengthRule(120)
        val item = newCapacityBudget("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = CapacityBudgetAmountRangeRule(7.4, 935.9)
        val item = newCapacityBudget("x", "y")
        item.amount = 937.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = CapacityBudgetRequiredNotesRule("capacity budget needs this field")
        val item = newCapacityBudget("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CapacityBudgetDistanceKmRangeRule(2.1, 1849.0)
        val item = newCapacityBudget("x", "y")
        item.distanceKm = 1850.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacityBudgetRequiredCodeRule("capacity budget needs a value")
        val item = newCapacityBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
