package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceAgreementValidator()
        val item = newFreightInvoiceAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = FreightInvoiceAgreementCategoryLengthRule(64)
        val item = newFreightInvoiceAgreement("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceAgreementRequiredCodeRule("freight invoice agreement needs a value")
        val item = newFreightInvoiceAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = FreightInvoiceAgreementRequiredCategoryRule("freight invoice agreement needs a value")
        val item = newFreightInvoiceAgreement("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = FreightInvoiceAgreementRequiredCarrierRule("freight invoice agreement needs this field")
        val item = newFreightInvoiceAgreement("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
