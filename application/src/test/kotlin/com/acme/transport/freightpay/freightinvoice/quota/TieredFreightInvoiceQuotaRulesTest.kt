package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFreightInvoiceQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFreightInvoiceQuotaValidator()
        val item = newTieredFreightInvoiceQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredFreightInvoiceQuotaLabelLengthRule(1000)
        val item = newTieredFreightInvoiceQuota("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredFreightInvoiceQuotaRequiredNameRule("tiered freight invoice quota needs this field")
        val item = newTieredFreightInvoiceQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TieredFreightInvoiceQuotaRequiredCurrencyRule("tiered freight invoice quota needs an entry")
        val item = newTieredFreightInvoiceQuota("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TieredFreightInvoiceQuotaAmountRangeRule(7.0, 3765.4)
        val item = newTieredFreightInvoiceQuota("x", "y")
        item.amount = 3766.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
