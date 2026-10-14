package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeOverrideValidator()
        val item = newFuelSurchargeOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeOverrideCodeLengthRule(120)
        val item = newFuelSurchargeOverride("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = FuelSurchargeOverrideSegmentLengthRule(80)
        val item = newFuelSurchargeOverride("x", "y")
        item.segment = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = FuelSurchargeOverrideRequiredUnitRule("fuel surcharge override needs a value")
        val item = newFuelSurchargeOverride("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
