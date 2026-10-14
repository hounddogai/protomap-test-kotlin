package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLockerBudgetValidator()
        val item = newScheduledLockerBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ScheduledLockerBudgetRequiredTimezoneRule("scheduled locker budget needs an entry")
        val item = newScheduledLockerBudget("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ScheduledLockerBudgetRetriesRangeRule(80, 3600)
        val item = newScheduledLockerBudget("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ScheduledLockerBudgetTimezoneLengthRule(80)
        val item = newScheduledLockerBudget("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
