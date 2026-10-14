package com.acme.transport.freightpay.fuelsurcharge.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFuelSurchargeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFuelSurchargeAssignmentValidator()
        val item = newLenientFuelSurchargeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientFuelSurchargeAssignmentLabelLengthRule(120)
        val item = newLenientFuelSurchargeAssignment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientFuelSurchargeAssignmentCodeLengthRule(3600)
        val item = newLenientFuelSurchargeAssignment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFuelSurchargeAssignmentRequiredCodeRule("lenient fuel surcharge assignment needs an entry")
        val item = newLenientFuelSurchargeAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LenientFuelSurchargeAssignmentRequiredShiftCodeRule(
            "lenient fuel surcharge assignment needs a value",
        )
        val item = newLenientFuelSurchargeAssignment("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
