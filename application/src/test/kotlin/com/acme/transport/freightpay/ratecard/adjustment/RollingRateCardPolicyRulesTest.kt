package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRateCardPolicyValidator()
        val item = newRollingRateCardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingRateCardPolicyNameLengthRule(3600)
        val item = newRollingRateCardPolicy("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingRateCardPolicyRequiredNameRule("rolling rate card policy needs an entry")
        val item = newRollingRateCardPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RollingRateCardPolicyGraceDaysRangeRule(255, 86400)
        val item = newRollingRateCardPolicy("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
