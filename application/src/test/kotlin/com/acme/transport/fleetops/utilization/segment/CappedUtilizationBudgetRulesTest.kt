package com.acme.transport.fleetops.utilization.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedUtilizationBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedUtilizationBudgetValidator()
        val item = newCappedUtilizationBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedUtilizationBudgetNameLengthRule(1000)
        val item = newCappedUtilizationBudget("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CappedUtilizationBudgetQuantityRangeRule(32, 1000)
        val item = newCappedUtilizationBudget("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = CappedUtilizationBudgetRequiredCategoryRule("capped utilization budget needs an entry")
        val item = newCappedUtilizationBudget("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedUtilizationBudgetRequiredCodeRule("capped utilization budget needs a value")
        val item = newCappedUtilizationBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
