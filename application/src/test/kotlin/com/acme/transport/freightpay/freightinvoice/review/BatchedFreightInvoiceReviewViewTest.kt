package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedFreightInvoiceReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedFreightInvoiceReview("code-599", "title-553")
        val view = toBatchedFreightInvoiceReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedFreightInvoiceReviewViews(mutableListOf<BatchedFreightInvoiceReview>(item, item)).size, "views")
    }
}
