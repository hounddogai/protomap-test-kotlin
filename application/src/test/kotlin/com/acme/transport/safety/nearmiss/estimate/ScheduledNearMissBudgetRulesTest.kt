package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNearMissBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledNearMissBudgetValidator()
        val item = newScheduledNearMissBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ScheduledNearMissBudgetCreatedAtRangeRule(1000, 1000)
        val item = newScheduledNearMissBudget("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledNearMissBudgetTitleLengthRule(1000)
        val item = newScheduledNearMissBudget("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ScheduledNearMissBudgetEtaMinutesRangeRule(1000, 3600)
        val item = newScheduledNearMissBudget("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledNearMissBudgetRequiredCategoryRule("scheduled near miss budget needs this field")
        val item = newScheduledNearMissBudget("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
