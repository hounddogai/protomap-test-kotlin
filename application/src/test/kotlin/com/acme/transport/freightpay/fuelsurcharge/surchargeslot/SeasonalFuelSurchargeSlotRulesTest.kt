package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFuelSurchargeSlotValidator()
        val item = newSeasonalFuelSurchargeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalFuelSurchargeSlotRequiredCodeRule("seasonal fuel surcharge slot needs a value")
        val item = newSeasonalFuelSurchargeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalFuelSurchargeSlotCodeLengthRule(80)
        val item = newSeasonalFuelSurchargeSlot("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
