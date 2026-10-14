package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFreightInvoiceRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingFreightInvoiceRequest("code-855", "title-429")
        assertEquals(RollingFreightInvoiceRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRollingFreightInvoiceRequest("code-855", "title-429")
        try {
            item.moveTo(RollingFreightInvoiceRequestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFreightInvoiceRequestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingFreightInvoiceRequest("code-855", "title-429")
        item.status = RollingFreightInvoiceRequestStatus.CLOSED
        assertFails("move") { item.moveTo(RollingFreightInvoiceRequestStatus.DRAFT) }
    }
}
