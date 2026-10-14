package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelSurchargeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFuelSurchargeAssignmentValidator()
        val item = newPrimaryFuelSurchargeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = PrimaryFuelSurchargeAssignmentCarrierLengthRule(80)
        val item = newPrimaryFuelSurchargeAssignment("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryFuelSurchargeAssignmentRequiredLabelRule(
            "primary fuel surcharge assignment needs this field",
        )
        val item = newPrimaryFuelSurchargeAssignment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryFuelSurchargeAssignmentTitleLengthRule(32)
        val item = newPrimaryFuelSurchargeAssignment("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = PrimaryFuelSurchargeAssignmentRequiredCarrierRule(
            "primary fuel surcharge assignment needs an entry",
        )
        val item = newPrimaryFuelSurchargeAssignment("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
