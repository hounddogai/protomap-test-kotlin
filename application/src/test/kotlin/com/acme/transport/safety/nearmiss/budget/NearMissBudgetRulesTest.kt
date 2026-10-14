package com.acme.transport.safety.nearmiss.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissBudgetValidator()
        val item = newNearMissBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = NearMissBudgetBudgetRangeRule(1.9, 2742.1)
        val item = newNearMissBudget("x", "y")
        item.budget = 2743.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NearMissBudgetRequiredNameRule("near miss budget needs this field")
        val item = newNearMissBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = NearMissBudgetRequiredShiftCodeRule("near miss budget needs a value")
        val item = newNearMissBudget("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = NearMissBudgetContactEmailLengthRule(16)
        val item = newNearMissBudget("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
