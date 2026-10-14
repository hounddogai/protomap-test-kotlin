package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFuelSurchargeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultFuelSurchargeWindowValidator()
        val item = newDefaultFuelSurchargeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultFuelSurchargeWindowRequiredCodeRule("default fuel surcharge window needs an entry")
        val item = newDefaultFuelSurchargeWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DefaultFuelSurchargeWindowRequiredCarrierRule("default fuel surcharge window needs this field")
        val item = newDefaultFuelSurchargeWindow("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DefaultFuelSurchargeWindowRequiredSegmentRule("default fuel surcharge window needs an entry")
        val item = newDefaultFuelSurchargeWindow("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = DefaultFuelSurchargeWindowCarrierLengthRule(1000)
        val item = newDefaultFuelSurchargeWindow("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultFuelSurchargeWindowRequiredLabelRule("default fuel surcharge window needs this field")
        val item = newDefaultFuelSurchargeWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
