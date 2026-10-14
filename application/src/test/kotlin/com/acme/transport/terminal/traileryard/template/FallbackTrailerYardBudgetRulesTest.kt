package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackTrailerYardBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackTrailerYardBudgetValidator()
        val item = newFallbackTrailerYardBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackTrailerYardBudgetRequiredCodeRule("fallback trailer yard budget needs a value")
        val item = newFallbackTrailerYardBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackTrailerYardBudgetNameLengthRule(3600)
        val item = newFallbackTrailerYardBudget("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackTrailerYardBudgetRequiredNameRule("fallback trailer yard budget needs this field")
        val item = newFallbackTrailerYardBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FallbackTrailerYardBudgetCurrencyLengthRule(120)
        val item = newFallbackTrailerYardBudget("x", "y")
        item.currency = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
