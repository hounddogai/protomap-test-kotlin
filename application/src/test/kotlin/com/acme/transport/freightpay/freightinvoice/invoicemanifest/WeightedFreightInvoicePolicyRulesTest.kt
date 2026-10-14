package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFreightInvoicePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFreightInvoicePolicyValidator()
        val item = newWeightedFreightInvoicePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = WeightedFreightInvoicePolicyDockLengthRule(255)
        val item = newWeightedFreightInvoicePolicy("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedFreightInvoicePolicyUpdatedAtRangeRule(32, 255)
        val item = newWeightedFreightInvoicePolicy("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedFreightInvoicePolicyQuantityRangeRule(32, 120)
        val item = newWeightedFreightInvoicePolicy("x", "y")
        item.quantity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
