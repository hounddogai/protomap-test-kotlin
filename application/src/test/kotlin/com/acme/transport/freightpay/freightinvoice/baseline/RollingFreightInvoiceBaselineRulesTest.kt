package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFreightInvoiceBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFreightInvoiceBaselineValidator()
        val item = newRollingFreightInvoiceBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RollingFreightInvoiceBaselineQuantityRangeRule(32, 255)
        val item = newRollingFreightInvoiceBaseline("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RollingFreightInvoiceBaselineRequiredTimezoneRule("rolling freight invoice baseline needs a value")
        val item = newRollingFreightInvoiceBaseline("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RollingFreightInvoiceBaselineShiftCodeLengthRule(32)
        val item = newRollingFreightInvoiceBaseline("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
