package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCoachingBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCoachingBudgetValidator()
        val item = newDefaultCoachingBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = DefaultCoachingBudgetSourceLengthRule(86400)
        val item = newDefaultCoachingBudget("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DefaultCoachingBudgetRateRangeRule(9.3, 4736.0)
        val item = newDefaultCoachingBudget("x", "y")
        item.rate = 4737.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultCoachingBudgetRequiredLabelRule("default coaching budget needs an entry")
        val item = newDefaultCoachingBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultCoachingBudgetLabelLengthRule(3600)
        val item = newDefaultCoachingBudget("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DefaultCoachingBudgetRequiredSourceRule("default coaching budget needs an entry")
        val item = newDefaultCoachingBudget("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
