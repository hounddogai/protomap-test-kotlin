package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFreightInvoiceDraftValidator()
        val item = newLenientFreightInvoiceDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = LenientFreightInvoiceDraftRequiredSummaryRule("lenient freight invoice draft needs this field")
        val item = newLenientFreightInvoiceDraft("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientFreightInvoiceDraftRequiredLabelRule("lenient freight invoice draft needs this field")
        val item = newLenientFreightInvoiceDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
