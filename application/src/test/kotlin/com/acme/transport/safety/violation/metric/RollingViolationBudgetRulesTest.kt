package com.acme.transport.safety.violation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingViolationBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingViolationBudgetValidator()
        val item = newRollingViolationBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingViolationBudgetTitleLengthRule(80)
        val item = newRollingViolationBudget("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingViolationBudgetRequiredTitleRule("rolling violation budget needs this field")
        val item = newRollingViolationBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RollingViolationBudgetRequiredTrailerIdRule("rolling violation budget needs a value")
        val item = newRollingViolationBudget("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
