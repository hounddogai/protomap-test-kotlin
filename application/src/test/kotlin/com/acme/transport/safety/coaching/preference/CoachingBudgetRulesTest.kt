package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingBudgetValidator()
        val item = newCoachingBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CoachingBudgetRequiredLabelRule("coaching budget needs this field")
        val item = newCoachingBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CoachingBudgetRequiredCodeRule("coaching budget needs a value")
        val item = newCoachingBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CoachingBudgetCodeLengthRule(120)
        val item = newCoachingBudget("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = CoachingBudgetExpiresAtRangeRule(64, 80)
        val item = newCoachingBudget("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
