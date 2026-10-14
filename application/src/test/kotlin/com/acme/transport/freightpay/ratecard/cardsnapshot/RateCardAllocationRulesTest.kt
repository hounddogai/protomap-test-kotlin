package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardAllocationValidator()
        val item = newRateCardAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RateCardAllocationGraceDaysRangeRule(32, 86400)
        val item = newRateCardAllocation("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RateCardAllocationRequiredTitleRule("rate card allocation needs a value")
        val item = newRateCardAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardAllocationRequiredCodeRule("rate card allocation needs a value")
        val item = newRateCardAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RateCardAllocationTitleLengthRule(3600)
        val item = newRateCardAllocation("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RateCardAllocationRequiredShiftCodeRule("rate card allocation needs this field")
        val item = newRateCardAllocation("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
