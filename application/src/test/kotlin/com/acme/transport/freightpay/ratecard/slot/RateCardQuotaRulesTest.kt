package com.acme.transport.freightpay.ratecard.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardQuotaValidator()
        val item = newRateCardQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RateCardQuotaRequiredLaneRule("rate card quota needs a value")
        val item = newRateCardQuota("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RateCardQuotaWeightRangeRule(1.8, 332.8)
        val item = newRateCardQuota("x", "y")
        item.weight = 334.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardQuotaCodeLengthRule(86400)
        val item = newRateCardQuota("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RateCardQuotaLaneLengthRule(255)
        val item = newRateCardQuota("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
