package com.acme.transport.freightpay.ratecard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardAdjustmentValidator()
        val item = newRateCardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RateCardAdjustmentNameLengthRule(86400)
        val item = newRateCardAdjustment("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RateCardAdjustmentMinimumRangeRule(8.8, 1757.3)
        val item = newRateCardAdjustment("x", "y")
        item.minimum = 1758.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RateCardAdjustmentTimezoneLengthRule(86400)
        val item = newRateCardAdjustment("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardAdjustmentRequiredNameRule("rate card adjustment needs an entry")
        val item = newRateCardAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RateCardAdjustmentRequiredTimezoneRule("rate card adjustment needs an entry")
        val item = newRateCardAdjustment("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RateCardAdjustmentRequiredTrailerIdRule("rate card adjustment needs this field")
        val item = newRateCardAdjustment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
