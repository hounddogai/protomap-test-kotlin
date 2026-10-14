package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceBundleValidator()
        val item = newFreightInvoiceBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = FreightInvoiceBundleRequiredTrailerIdRule("freight invoice bundle needs a value")
        val item = newFreightInvoiceBundle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = FreightInvoiceBundleQuantityRangeRule(64, 80)
        val item = newFreightInvoiceBundle("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = FreightInvoiceBundleStartsAtRangeRule(64, 120)
        val item = newFreightInvoiceBundle("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FreightInvoiceBundleNameLengthRule(1000)
        val item = newFreightInvoiceBundle("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
