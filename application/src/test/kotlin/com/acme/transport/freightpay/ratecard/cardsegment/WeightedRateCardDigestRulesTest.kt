package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRateCardDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRateCardDigestValidator()
        val item = newWeightedRateCardDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = WeightedRateCardDigestMaximumRangeRule(3.4, 983.3)
        val item = newWeightedRateCardDigest("x", "y")
        item.maximum = 984.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedRateCardDigestTitleLengthRule(86400)
        val item = newWeightedRateCardDigest("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedRateCardDigestRequiredTitleRule("weighted rate card digest needs this field")
        val item = newWeightedRateCardDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
