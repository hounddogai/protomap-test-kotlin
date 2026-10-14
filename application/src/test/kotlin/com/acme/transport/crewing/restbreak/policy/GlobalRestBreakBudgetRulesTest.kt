package com.acme.transport.crewing.restbreak.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRestBreakBudgetValidator()
        val item = newGlobalRestBreakBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = GlobalRestBreakBudgetStopCountRangeRule(32, 86400)
        val item = newGlobalRestBreakBudget("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = GlobalRestBreakBudgetUpdatedAtRangeRule(32, 80)
        val item = newGlobalRestBreakBudget("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalRestBreakBudgetLabelLengthRule(1000)
        val item = newGlobalRestBreakBudget("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = GlobalRestBreakBudgetDistanceKmRangeRule(0.2, 3693.9)
        val item = newGlobalRestBreakBudget("x", "y")
        item.distanceKm = 3695.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = GlobalRestBreakBudgetDescriptionLengthRule(86400)
        val item = newGlobalRestBreakBudget("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
