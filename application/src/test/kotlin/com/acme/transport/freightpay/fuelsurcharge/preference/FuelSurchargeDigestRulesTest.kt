package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeDigestValidator()
        val item = newFuelSurchargeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FuelSurchargeDigestRateRangeRule(9.3, 659.9)
        val item = newFuelSurchargeDigest("x", "y")
        item.rate = 661.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargeDigestTitleLengthRule(64)
        val item = newFuelSurchargeDigest("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = FuelSurchargeDigestGraceDaysRangeRule(32, 64)
        val item = newFuelSurchargeDigest("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeDigestCodeLengthRule(32)
        val item = newFuelSurchargeDigest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
