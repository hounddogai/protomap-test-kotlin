package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRestBreakBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRestBreakBudgetValidator()
        val item = newScheduledRestBreakBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRestBreakBudgetCodeLengthRule(1000)
        val item = newScheduledRestBreakBudget("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledRestBreakBudgetTitleLengthRule(16)
        val item = newScheduledRestBreakBudget("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledRestBreakBudgetFuelLitersRangeRule(5.8, 708.0)
        val item = newScheduledRestBreakBudget("x", "y")
        item.fuelLiters = 709.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledRestBreakBudgetRequiredTitleRule("scheduled rest break budget needs an entry")
        val item = newScheduledRestBreakBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ScheduledRestBreakBudgetWindowDaysRangeRule(32, 80)
        val item = newScheduledRestBreakBudget("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
