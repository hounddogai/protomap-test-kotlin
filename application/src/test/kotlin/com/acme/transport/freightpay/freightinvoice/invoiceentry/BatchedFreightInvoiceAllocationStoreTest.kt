package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedFreightInvoiceAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedFreightInvoiceAllocationStore()
        val item = newBatchedFreightInvoiceAllocation("code-908", "label-507")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
