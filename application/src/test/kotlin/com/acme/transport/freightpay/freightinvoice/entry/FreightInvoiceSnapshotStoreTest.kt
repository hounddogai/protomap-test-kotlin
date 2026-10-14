package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceSnapshotStore()
        val item = newFreightInvoiceSnapshot("code-181", "name-582")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
