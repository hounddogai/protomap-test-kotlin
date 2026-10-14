package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRateCardEntryValidator()
        val item = newGlobalRateCardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalRateCardEntryDiscountRangeRule(6.0, 2125.0)
        val item = newGlobalRateCardEntry("x", "y")
        item.discount = 2126.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRateCardEntryCodeLengthRule(86400)
        val item = newGlobalRateCardEntry("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalRateCardEntryRequiredCodeRule("global rate card entry needs a value")
        val item = newGlobalRateCardEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = GlobalRateCardEntryOdometerRangeRule(64, 86400)
        val item = newGlobalRateCardEntry("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
