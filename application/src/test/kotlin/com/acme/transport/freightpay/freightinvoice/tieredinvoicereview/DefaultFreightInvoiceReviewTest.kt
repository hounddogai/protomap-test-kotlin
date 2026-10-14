package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultFreightInvoiceReviewTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultFreightInvoiceReview("code-967", "label-187")
        assertEquals(DefaultFreightInvoiceReviewStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultFreightInvoiceReview("code-967", "label-187")
        try {
            item.moveTo(DefaultFreightInvoiceReviewStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultFreightInvoiceReviewStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultFreightInvoiceReview("code-967", "label-187")
        item.status = DefaultFreightInvoiceReviewStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultFreightInvoiceReviewStatus.REQUESTED) }
    }
}
