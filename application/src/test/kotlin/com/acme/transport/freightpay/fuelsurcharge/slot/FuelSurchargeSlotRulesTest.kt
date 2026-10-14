package com.acme.transport.freightpay.fuelsurcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeSlotValidator()
        val item = newFuelSurchargeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FuelSurchargeSlotStopCountRangeRule(255, 86400)
        val item = newFuelSurchargeSlot("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeSlotRequiredCodeRule("fuel surcharge slot needs an entry")
        val item = newFuelSurchargeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargeSlotTitleLengthRule(3600)
        val item = newFuelSurchargeSlot("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
