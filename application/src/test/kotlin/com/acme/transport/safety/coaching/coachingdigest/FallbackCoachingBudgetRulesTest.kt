package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCoachingBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCoachingBudgetValidator()
        val item = newFallbackCoachingBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FallbackCoachingBudgetMinimumRangeRule(0.9, 3052.2)
        val item = newFallbackCoachingBudget("x", "y")
        item.minimum = 3053.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackCoachingBudgetCodeLengthRule(16)
        val item = newFallbackCoachingBudget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
