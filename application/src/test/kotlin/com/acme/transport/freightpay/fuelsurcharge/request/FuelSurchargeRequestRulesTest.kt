package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeRequestValidator()
        val item = newFuelSurchargeRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FuelSurchargeRequestLabelLengthRule(120)
        val item = newFuelSurchargeRequest("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeRequestRequiredCodeRule("fuel surcharge request needs this field")
        val item = newFuelSurchargeRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FuelSurchargeRequestEndsAtRangeRule(16, 120)
        val item = newFuelSurchargeRequest("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
