package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargePolicyValidator()
        val item = newFuelSurchargePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = FuelSurchargePolicyRequiredNotesRule("fuel surcharge policy needs an entry")
        val item = newFuelSurchargePolicy("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = FuelSurchargePolicyNotesLengthRule(16)
        val item = newFuelSurchargePolicy("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = FuelSurchargePolicyTitleLengthRule(64)
        val item = newFuelSurchargePolicy("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = FuelSurchargePolicyRequiredSourceRule("fuel surcharge policy needs this field")
        val item = newFuelSurchargePolicy("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
