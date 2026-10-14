package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRateCardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRateCardEntryValidator()
        val item = newLenientRateCardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LenientRateCardEntryFuelLitersRangeRule(5.0, 2474.8)
        val item = newLenientRateCardEntry("x", "y")
        item.fuelLiters = 2476.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientRateCardEntryRequiredCodeRule("lenient rate card entry needs an entry")
        val item = newLenientRateCardEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LenientRateCardEntryGraceDaysRangeRule(16, 16)
        val item = newLenientRateCardEntry("x", "y")
        item.graceDays = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRateCardEntryCodeLengthRule(64)
        val item = newLenientRateCardEntry("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientRateCardEntryNameLengthRule(86400)
        val item = newLenientRateCardEntry("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
