package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceReviewTest {
    @Test
    fun `starts in proposed`() {
        val item = newFreightInvoiceReview("code-269", "name-501")
        assertEquals(FreightInvoiceReviewStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFreightInvoiceReview("code-269", "name-501")
        try {
            item.moveTo(FreightInvoiceReviewStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceReviewStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFreightInvoiceReview("code-269", "name-501")
        item.status = FreightInvoiceReviewStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FreightInvoiceReviewStatus.PROPOSED) }
    }
}
