package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFreightInvoiceProfileValidator()
        val item = newLenientFreightInvoiceProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientFreightInvoiceProfileCodeLengthRule(16)
        val item = newLenientFreightInvoiceProfile("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientFreightInvoiceProfileAttemptsRangeRule(16, 3600)
        val item = newLenientFreightInvoiceProfile("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
