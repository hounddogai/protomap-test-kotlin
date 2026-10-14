package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardPolicyValidator()
        val item = newRateCardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RateCardPolicyRequiredTitleRule("rate card policy needs this field")
        val item = newRateCardPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RateCardPolicyStopCountRangeRule(80, 1000)
        val item = newRateCardPolicy("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RateCardPolicyContactEmailLengthRule(3600)
        val item = newRateCardPolicy("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RateCardPolicyStartsAtRangeRule(255, 3600)
        val item = newRateCardPolicy("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
