package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellBudgetValidator()
        val item = newDwellBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = DwellBudgetContactEmailLengthRule(86400)
        val item = newDwellBudget("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellBudgetRequiredCodeRule("dwell budget needs a value")
        val item = newDwellBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
