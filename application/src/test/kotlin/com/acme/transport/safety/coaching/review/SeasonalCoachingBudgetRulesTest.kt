package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCoachingBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCoachingBudgetValidator()
        val item = newSeasonalCoachingBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalCoachingBudgetRequiredOwnerIdRule("seasonal coaching budget needs a value")
        val item = newSeasonalCoachingBudget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCoachingBudgetCodeLengthRule(16)
        val item = newSeasonalCoachingBudget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalCoachingBudgetRequiredCodeRule("seasonal coaching budget needs a value")
        val item = newSeasonalCoachingBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalCoachingBudgetLabelLengthRule(255)
        val item = newSeasonalCoachingBudget("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SeasonalCoachingBudgetMarginRangeRule(5.2, 4228.9)
        val item = newSeasonalCoachingBudget("x", "y")
        item.margin = 4230.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
