package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFreightInvoiceOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedFreightInvoiceOverride("code-814", "label-104")
        assertEquals(BatchedFreightInvoiceOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedFreightInvoiceOverride("code-814", "label-104")
        try {
            item.moveTo(BatchedFreightInvoiceOverrideStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFreightInvoiceOverrideStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedFreightInvoiceOverride("code-814", "label-104")
        item.status = BatchedFreightInvoiceOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedFreightInvoiceOverrideStatus.PLANNED) }
    }
}
