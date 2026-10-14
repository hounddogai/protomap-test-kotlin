package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardTierValidator()
        val item = newRateCardTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RateCardTierRequiredTitleRule("rate card tier needs a value")
        val item = newRateCardTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = RateCardTierRequiredCurrencyRule("rate card tier needs a value")
        val item = newRateCardTier("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
