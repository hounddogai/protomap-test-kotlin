package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingFreightInvoiceSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingFreightInvoiceSummaryStore()
        val item = newPendingFreightInvoiceSummary("code-655", "label-301")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
