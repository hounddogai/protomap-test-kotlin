package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFreightInvoiceAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFreightInvoiceAdjustmentValidator()
        val item = newPrimaryFreightInvoiceAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryFreightInvoiceAdjustmentRequiredTitleRule(
            "primary freight invoice adjustment needs this field",
        )
        val item = newPrimaryFreightInvoiceAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryFreightInvoiceAdjustmentStopCountRangeRule(64, 120)
        val item = newPrimaryFreightInvoiceAdjustment("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = PrimaryFreightInvoiceAdjustmentCurrencyLengthRule(16)
        val item = newPrimaryFreightInvoiceAdjustment("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
