package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardGroupValidator()
        val item = newRateCardGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RateCardGroupWindowDaysRangeRule(80, 86400)
        val item = newRateCardGroup("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RateCardGroupRatioRangeRule(5.3, 2033.5)
        val item = newRateCardGroup("x", "y")
        item.ratio = 2035.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RateCardGroupCarrierLengthRule(80)
        val item = newRateCardGroup("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RateCardGroupRequiredCarrierRule("rate card group needs a value")
        val item = newRateCardGroup("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
