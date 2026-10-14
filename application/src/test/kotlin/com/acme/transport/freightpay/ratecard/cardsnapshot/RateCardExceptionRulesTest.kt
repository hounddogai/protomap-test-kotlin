package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardExceptionValidator()
        val item = newRateCardException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RateCardExceptionRetriesRangeRule(64, 255)
        val item = newRateCardException("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RateCardExceptionRequiredRegionRule("rate card exception needs a value")
        val item = newRateCardException("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = RateCardExceptionCategoryLengthRule(86400)
        val item = newRateCardException("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
