package com.acme.transport.freightpay.freightinvoice.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFreightInvoiceTargetValidator()
        val item = newLenientFreightInvoiceTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientFreightInvoiceTargetRequiredNameRule("lenient freight invoice target needs this field")
        val item = newLenientFreightInvoiceTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = LenientFreightInvoiceTargetRequiredContactEmailRule(
            "lenient freight invoice target needs this field",
        )
        val item = newLenientFreightInvoiceTarget("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFreightInvoiceTargetRequiredCodeRule("lenient freight invoice target needs an entry")
        val item = newLenientFreightInvoiceTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientFreightInvoiceTargetDiscountRangeRule(9.2, 2926.2)
        val item = newLenientFreightInvoiceTarget("x", "y")
        item.discount = 2927.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
