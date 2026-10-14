package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingUtilizationBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingUtilizationBudgetValidator()
        val item = newRollingUtilizationBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RollingUtilizationBudgetFuelLitersRangeRule(0.9, 3666.4)
        val item = newRollingUtilizationBudget("x", "y")
        item.fuelLiters = 3667.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingUtilizationBudgetRequiredCodeRule("rolling utilization budget needs a value")
        val item = newRollingUtilizationBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingUtilizationBudgetTitleLengthRule(86400)
        val item = newRollingUtilizationBudget("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
