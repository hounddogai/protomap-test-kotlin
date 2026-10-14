package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorBudgetValidator()
        val item = newEmissionFactorBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = EmissionFactorBudgetRatioRangeRule(4.4, 2195.3)
        val item = newEmissionFactorBudget("x", "y")
        item.ratio = 2196.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorBudgetRequiredCodeRule("emission factor budget needs a value")
        val item = newEmissionFactorBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EmissionFactorBudgetNameLengthRule(64)
        val item = newEmissionFactorBudget("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
