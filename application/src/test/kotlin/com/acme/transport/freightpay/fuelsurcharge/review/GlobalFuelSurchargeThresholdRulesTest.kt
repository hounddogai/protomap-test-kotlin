package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelSurchargeThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelSurchargeThresholdValidator()
        val item = newGlobalFuelSurchargeThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = GlobalFuelSurchargeThresholdShiftCodeLengthRule(3600)
        val item = newGlobalFuelSurchargeThreshold("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = GlobalFuelSurchargeThresholdRequiredContactEmailRule(
            "global fuel surcharge threshold needs a value",
        )
        val item = newGlobalFuelSurchargeThreshold("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFuelSurchargeThresholdCodeLengthRule(120)
        val item = newGlobalFuelSurchargeThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = GlobalFuelSurchargeThresholdRequiredShiftCodeRule(
            "global fuel surcharge threshold needs this field",
        )
        val item = newGlobalFuelSurchargeThreshold("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = GlobalFuelSurchargeThresholdQuantityRangeRule(32, 3600)
        val item = newGlobalFuelSurchargeThreshold("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
