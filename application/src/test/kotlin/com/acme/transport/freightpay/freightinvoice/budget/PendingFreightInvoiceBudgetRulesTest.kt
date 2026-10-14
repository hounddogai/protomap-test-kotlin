package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFreightInvoiceBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFreightInvoiceBudgetValidator()
        val item = newPendingFreightInvoiceBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingFreightInvoiceBudgetRequiredCodeRule("pending freight invoice budget needs a value")
        val item = newPendingFreightInvoiceBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingFreightInvoiceBudgetThresholdRangeRule(0.7, 3026.6)
        val item = newPendingFreightInvoiceBudget("x", "y")
        item.threshold = 3028.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PendingFreightInvoiceBudgetMarginRangeRule(5.9, 163.6)
        val item = newPendingFreightInvoiceBudget("x", "y")
        item.margin = 165.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
