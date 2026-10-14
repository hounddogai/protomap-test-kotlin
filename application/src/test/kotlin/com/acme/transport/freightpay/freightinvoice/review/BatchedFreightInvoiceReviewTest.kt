package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFreightInvoiceReviewTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedFreightInvoiceReview("code-599", "title-553")
        assertEquals(BatchedFreightInvoiceReviewStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedFreightInvoiceReview("code-599", "title-553")
        try {
            item.moveTo(BatchedFreightInvoiceReviewStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFreightInvoiceReviewStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedFreightInvoiceReview("code-599", "title-553")
        item.status = BatchedFreightInvoiceReviewStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedFreightInvoiceReviewStatus.OPEN) }
    }
}
