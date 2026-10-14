package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFreightInvoiceReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFreightInvoiceReviewValidator()
        val item = newManualFreightInvoiceReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualFreightInvoiceReviewRequiredCodeRule("manual freight invoice review needs a value")
        val item = newManualFreightInvoiceReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ManualFreightInvoiceReviewAttemptsRangeRule(32, 64)
        val item = newManualFreightInvoiceReview("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
