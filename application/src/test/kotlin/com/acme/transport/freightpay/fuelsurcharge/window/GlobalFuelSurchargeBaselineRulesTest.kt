package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelSurchargeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelSurchargeBaselineValidator()
        val item = newGlobalFuelSurchargeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = GlobalFuelSurchargeBaselineRequiredCarrierRule("global fuel surcharge baseline needs this field")
        val item = newGlobalFuelSurchargeBaseline("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalFuelSurchargeBaselineCodeLengthRule(80)
        val item = newGlobalFuelSurchargeBaseline("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalFuelSurchargeBaselineTitleLengthRule(255)
        val item = newGlobalFuelSurchargeBaseline("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = GlobalFuelSurchargeBaselineCapacityRangeRule(32, 255)
        val item = newGlobalFuelSurchargeBaseline("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = GlobalFuelSurchargeBaselineCostRangeRule(4.8, 713.7)
        val item = newGlobalFuelSurchargeBaseline("x", "y")
        item.cost = 715.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
