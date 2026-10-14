package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictFreightInvoiceRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictFreightInvoiceRequest("code-216", "name-859")
        assertEquals(StrictFreightInvoiceRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newStrictFreightInvoiceRequest("code-216", "name-859")
        try {
            item.moveTo(StrictFreightInvoiceRequestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictFreightInvoiceRequestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictFreightInvoiceRequest("code-216", "name-859")
        item.status = StrictFreightInvoiceRequestStatus.CLOSED
        assertFails("move") { item.moveTo(StrictFreightInvoiceRequestStatus.DRAFT) }
    }
}
