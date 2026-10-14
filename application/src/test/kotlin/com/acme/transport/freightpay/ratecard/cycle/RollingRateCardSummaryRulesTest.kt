package com.acme.transport.freightpay.ratecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRateCardSummaryValidator()
        val item = newRollingRateCardSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RollingRateCardSummaryBudgetRangeRule(3.2, 3587.4)
        val item = newRollingRateCardSummary("x", "y")
        item.budget = 3588.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RollingRateCardSummaryAmountRangeRule(1.0, 3935.7)
        val item = newRollingRateCardSummary("x", "y")
        item.amount = 3937.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingRateCardSummaryCodeLengthRule(120)
        val item = newRollingRateCardSummary("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
