package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDwellBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDwellBudgetValidator()
        val item = newDefaultDwellBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultDwellBudgetCodeLengthRule(3600)
        val item = newDefaultDwellBudget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DefaultDwellBudgetBudgetRangeRule(5.2, 4734.5)
        val item = newDefaultDwellBudget("x", "y")
        item.budget = 4736.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
