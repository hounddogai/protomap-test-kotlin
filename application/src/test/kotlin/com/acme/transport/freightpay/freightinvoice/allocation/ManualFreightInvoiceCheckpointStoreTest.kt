package com.acme.transport.freightpay.freightinvoice.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualFreightInvoiceCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualFreightInvoiceCheckpointStore()
        val item = newManualFreightInvoiceCheckpoint("code-194", "name-689")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
