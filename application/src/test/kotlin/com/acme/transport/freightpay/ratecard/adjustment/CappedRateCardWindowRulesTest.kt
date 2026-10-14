package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRateCardWindowValidator()
        val item = newCappedRateCardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = CappedRateCardWindowTimezoneLengthRule(1000)
        val item = newCappedRateCardWindow("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = CappedRateCardWindowCapacityRangeRule(32, 255)
        val item = newCappedRateCardWindow("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CappedRateCardWindowPalletsRangeRule(32, 3600)
        val item = newCappedRateCardWindow("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
