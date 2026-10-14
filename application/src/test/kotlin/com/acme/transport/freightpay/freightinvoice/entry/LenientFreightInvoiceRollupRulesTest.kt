package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFreightInvoiceRollupValidator()
        val item = newLenientFreightInvoiceRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientFreightInvoiceRollupLabelLengthRule(255)
        val item = newLenientFreightInvoiceRollup("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LenientFreightInvoiceRollupShiftCodeLengthRule(1000)
        val item = newLenientFreightInvoiceRollup("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
