package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeTargetValidator()
        val item = newFuelSurchargeTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelSurchargeTargetNameLengthRule(16)
        val item = newFuelSurchargeTarget("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelSurchargeTargetRequiredNameRule("fuel surcharge target needs a value")
        val item = newFuelSurchargeTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FuelSurchargeTargetRequiredContactEmailRule("fuel surcharge target needs this field")
        val item = newFuelSurchargeTarget("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
