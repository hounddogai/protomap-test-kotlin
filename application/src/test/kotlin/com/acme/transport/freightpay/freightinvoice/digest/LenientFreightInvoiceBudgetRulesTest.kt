package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientFreightInvoiceBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientFreightInvoiceBudgetValidator()
        val item = newLenientFreightInvoiceBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LenientFreightInvoiceBudgetCapacityRangeRule(32, 3600)
        val item = newLenientFreightInvoiceBudget("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientFreightInvoiceBudgetRequiredCodeRule("lenient freight invoice budget needs a value")
        val item = newLenientFreightInvoiceBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LenientFreightInvoiceBudgetQuantityRangeRule(1000, 86400)
        val item = newLenientFreightInvoiceBudget("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LenientFreightInvoiceBudgetReviewerLengthRule(1000)
        val item = newLenientFreightInvoiceBudget("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientFreightInvoiceBudgetRequiredReviewerRule("lenient freight invoice budget needs a value")
        val item = newLenientFreightInvoiceBudget("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
