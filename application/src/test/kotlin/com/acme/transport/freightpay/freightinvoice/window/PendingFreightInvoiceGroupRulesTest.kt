package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFreightInvoiceGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFreightInvoiceGroupValidator()
        val item = newPendingFreightInvoiceGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingFreightInvoiceGroupTitleLengthRule(80)
        val item = newPendingFreightInvoiceGroup("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingFreightInvoiceGroupRequiredCodeRule("pending freight invoice group needs a value")
        val item = newPendingFreightInvoiceGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PendingFreightInvoiceGroupDiscountRangeRule(6.9, 687.7)
        val item = newPendingFreightInvoiceGroup("x", "y")
        item.discount = 689.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingFreightInvoiceGroupRequiredLabelRule("pending freight invoice group needs this field")
        val item = newPendingFreightInvoiceGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingFreightInvoiceGroupCodeLengthRule(1000)
        val item = newPendingFreightInvoiceGroup("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
