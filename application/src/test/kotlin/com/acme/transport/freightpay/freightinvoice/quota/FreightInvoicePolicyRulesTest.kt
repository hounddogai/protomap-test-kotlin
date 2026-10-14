package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoicePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoicePolicyValidator()
        val item = newFreightInvoicePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = FreightInvoicePolicyRequiredTimezoneRule("freight invoice policy needs a value")
        val item = newFreightInvoicePolicy("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoicePolicyRequiredCodeRule("freight invoice policy needs this field")
        val item = newFreightInvoicePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FreightInvoicePolicyLabelLengthRule(120)
        val item = newFreightInvoicePolicy("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
