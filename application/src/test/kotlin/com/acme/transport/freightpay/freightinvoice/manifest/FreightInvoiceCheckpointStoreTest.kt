package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceCheckpointStore()
        val item = newFreightInvoiceCheckpoint("code-892", "name-888")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
