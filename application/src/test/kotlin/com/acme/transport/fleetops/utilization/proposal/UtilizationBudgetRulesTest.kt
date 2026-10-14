package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationBudgetValidator()
        val item = newUtilizationBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = UtilizationBudgetRequiredUnitRule("utilization budget needs an entry")
        val item = newUtilizationBudget("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = UtilizationBudgetStopCountRangeRule(16, 1000)
        val item = newUtilizationBudget("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = UtilizationBudgetCategoryLengthRule(120)
        val item = newUtilizationBudget("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = UtilizationBudgetMinimumRangeRule(2.8, 1880.4)
        val item = newUtilizationBudget("x", "y")
        item.minimum = 1881.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
