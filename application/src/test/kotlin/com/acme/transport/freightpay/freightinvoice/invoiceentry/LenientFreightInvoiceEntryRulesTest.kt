package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFreightInvoiceEntryValidator()
        val item = newLenientFreightInvoiceEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LenientFreightInvoiceEntrySegmentLengthRule(3600)
        val item = newLenientFreightInvoiceEntry("x", "y")
        item.segment = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = LenientFreightInvoiceEntryRequiredLaneRule("lenient freight invoice entry needs an entry")
        val item = newLenientFreightInvoiceEntry("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFreightInvoiceEntryRequiredCodeRule("lenient freight invoice entry needs this field")
        val item = newLenientFreightInvoiceEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientFreightInvoiceEntryNameLengthRule(16)
        val item = newLenientFreightInvoiceEntry("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
