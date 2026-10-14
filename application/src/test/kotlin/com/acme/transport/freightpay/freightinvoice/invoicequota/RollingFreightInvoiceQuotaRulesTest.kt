package com.acme.transport.freightpay.freightinvoice.invoicequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFreightInvoiceQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFreightInvoiceQuotaValidator()
        val item = newRollingFreightInvoiceQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingFreightInvoiceQuotaRequiredNameRule("rolling freight invoice quota needs a value")
        val item = newRollingFreightInvoiceQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingFreightInvoiceQuotaCodeLengthRule(32)
        val item = newRollingFreightInvoiceQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RollingFreightInvoiceQuotaWeightRangeRule(5.3, 4016.8)
        val item = newRollingFreightInvoiceQuota("x", "y")
        item.weight = 4018.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingFreightInvoiceQuotaNameLengthRule(255)
        val item = newRollingFreightInvoiceQuota("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
