package com.acme.transport.freightpay.freightinvoice.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackFreightInvoiceCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackFreightInvoiceCheckpointStore()
        val item = newFallbackFreightInvoiceCheckpoint("code-325", "name-731")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
