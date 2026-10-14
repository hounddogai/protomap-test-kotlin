package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFreightInvoiceAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedFreightInvoiceAllocation("code-908", "label-507")
        assertEquals(BatchedFreightInvoiceAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newBatchedFreightInvoiceAllocation("code-908", "label-507")
        try {
            item.moveTo(BatchedFreightInvoiceAllocationStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFreightInvoiceAllocationStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedFreightInvoiceAllocation("code-908", "label-507")
        item.status = BatchedFreightInvoiceAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedFreightInvoiceAllocationStatus.REQUESTED) }
    }
}
