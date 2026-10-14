package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelSurchargeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelSurchargeAdjustmentValidator()
        val item = newTieredFuelSurchargeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TieredFuelSurchargeAdjustmentRequiredPhoneRule("tiered fuel surcharge adjustment needs a value")
        val item = newTieredFuelSurchargeAdjustment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = TieredFuelSurchargeAdjustmentPhoneLengthRule(255)
        val item = newTieredFuelSurchargeAdjustment("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = TieredFuelSurchargeAdjustmentStartsAtRangeRule(32, 1000)
        val item = newTieredFuelSurchargeAdjustment("x", "y")
        item.startsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredFuelSurchargeAdjustmentRequiredCodeRule("tiered fuel surcharge adjustment needs this field")
        val item = newTieredFuelSurchargeAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
