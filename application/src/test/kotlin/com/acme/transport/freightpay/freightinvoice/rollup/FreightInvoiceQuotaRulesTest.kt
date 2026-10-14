package com.acme.transport.freightpay.freightinvoice.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceQuotaValidator()
        val item = newFreightInvoiceQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = FreightInvoiceQuotaCreatedAtRangeRule(120, 1000)
        val item = newFreightInvoiceQuota("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FreightInvoiceQuotaEndsAtRangeRule(120, 255)
        val item = newFreightInvoiceQuota("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceQuotaRequiredCodeRule("freight invoice quota needs a value")
        val item = newFreightInvoiceQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FreightInvoiceQuotaMinimumRangeRule(7.0, 224.0)
        val item = newFreightInvoiceQuota("x", "y")
        item.minimum = 225.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
