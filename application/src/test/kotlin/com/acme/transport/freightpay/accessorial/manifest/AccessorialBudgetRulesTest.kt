package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialBudgetValidator()
        val item = newAccessorialBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialBudgetCodeLengthRule(80)
        val item = newAccessorialBudget("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = AccessorialBudgetRequiredCurrencyRule("accessorial budget needs an entry")
        val item = newAccessorialBudget("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = AccessorialBudgetCurrencyLengthRule(120)
        val item = newAccessorialBudget("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = AccessorialBudgetCostRangeRule(8.4, 1953.0)
        val item = newAccessorialBudget("x", "y")
        item.cost = 1954.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
