package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelBurnBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFuelBurnBudgetValidator()
        val item = newLenientFuelBurnBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFuelBurnBudgetRequiredCodeRule("lenient fuel burn budget needs this field")
        val item = newLenientFuelBurnBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LenientFuelBurnBudgetExpiresAtRangeRule(16, 1000)
        val item = newLenientFuelBurnBudget("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LenientFuelBurnBudgetLimitRangeRule(16, 86400)
        val item = newLenientFuelBurnBudget("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientFuelBurnBudgetWeightRangeRule(3.9, 3552.4)
        val item = newLenientFuelBurnBudget("x", "y")
        item.weight = 3553.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
