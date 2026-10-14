package com.acme.transport.freightpay.freightinvoice.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newFreightInvoiceException("code-604", "title-597")
        assertEquals(FreightInvoiceExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newFreightInvoiceException("code-604", "title-597")
        try {
            item.moveTo(FreightInvoiceExceptionStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceExceptionStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFreightInvoiceException("code-604", "title-597")
        item.status = FreightInvoiceExceptionStatus.FAILED
        assertFails("move") { item.moveTo(FreightInvoiceExceptionStatus.NEW) }
    }
}
