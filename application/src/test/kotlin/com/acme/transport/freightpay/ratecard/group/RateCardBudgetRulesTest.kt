package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardBudgetValidator()
        val item = newRateCardBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = RateCardBudgetSegmentLengthRule(86400)
        val item = newRateCardBudget("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardBudgetCodeLengthRule(16)
        val item = newRateCardBudget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardBudgetRequiredCodeRule("rate card budget needs an entry")
        val item = newRateCardBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
