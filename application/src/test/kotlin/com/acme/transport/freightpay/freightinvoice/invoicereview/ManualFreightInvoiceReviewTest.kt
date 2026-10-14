package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFreightInvoiceReviewTest {
    @Test
    fun `starts in planned`() {
        val item = newManualFreightInvoiceReview("code-623", "name-999")
        assertEquals(ManualFreightInvoiceReviewStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newManualFreightInvoiceReview("code-623", "name-999")
        try {
            item.moveTo(ManualFreightInvoiceReviewStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFreightInvoiceReviewStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualFreightInvoiceReview("code-623", "name-999")
        item.status = ManualFreightInvoiceReviewStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualFreightInvoiceReviewStatus.PLANNED) }
    }
}
