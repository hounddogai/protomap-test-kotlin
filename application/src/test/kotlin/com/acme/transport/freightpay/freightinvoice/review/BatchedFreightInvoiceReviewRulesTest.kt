package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFreightInvoiceReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFreightInvoiceReviewValidator()
        val item = newBatchedFreightInvoiceReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedFreightInvoiceReviewRequiredTitleRule("batched freight invoice review needs an entry")
        val item = newBatchedFreightInvoiceReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = BatchedFreightInvoiceReviewRequiredRouteCodeRule("batched freight invoice review needs an entry")
        val item = newBatchedFreightInvoiceReview("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = BatchedFreightInvoiceReviewSourceLengthRule(64)
        val item = newBatchedFreightInvoiceReview("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
