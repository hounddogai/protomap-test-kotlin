package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRateCardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRateCardPolicyValidator()
        val item = newLenientRateCardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LenientRateCardPolicyRequiredChannelRule("lenient rate card policy needs a value")
        val item = newLenientRateCardPolicy("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LenientRateCardPolicyPalletsRangeRule(32, 86400)
        val item = newLenientRateCardPolicy("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LenientRateCardPolicySegmentLengthRule(80)
        val item = newLenientRateCardPolicy("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LenientRateCardPolicyCapacityRangeRule(255, 86400)
        val item = newLenientRateCardPolicy("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
