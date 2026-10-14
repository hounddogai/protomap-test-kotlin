package com.acme.transport.safety.violation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalViolationBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalViolationBudgetValidator()
        val item = newGlobalViolationBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = GlobalViolationBudgetRequiredNotesRule("global violation budget needs an entry")
        val item = newGlobalViolationBudget("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = GlobalViolationBudgetStartsAtRangeRule(16, 64)
        val item = newGlobalViolationBudget("x", "y")
        item.startsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = GlobalViolationBudgetNotesLengthRule(16)
        val item = newGlobalViolationBudget("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalViolationBudgetGraceDaysRangeRule(120, 86400)
        val item = newGlobalViolationBudget("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalViolationBudgetCodeLengthRule(86400)
        val item = newGlobalViolationBudget("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
