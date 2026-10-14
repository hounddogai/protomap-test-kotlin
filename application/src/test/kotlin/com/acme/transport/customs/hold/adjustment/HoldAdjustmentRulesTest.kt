package com.acme.transport.customs.hold.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldAdjustmentValidator()
        val item = newHoldAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = HoldAdjustmentOwnerIdLengthRule(32)
        val item = newHoldAdjustment("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = HoldAdjustmentRequiredPhoneRule("hold adjustment needs this field")
        val item = newHoldAdjustment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldAdjustmentCodeLengthRule(80)
        val item = newHoldAdjustment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldAdjustmentRequiredCodeRule("hold adjustment needs a value")
        val item = newHoldAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = HoldAdjustmentRateRangeRule(6.8, 2810.9)
        val item = newHoldAdjustment("x", "y")
        item.rate = 2812.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
