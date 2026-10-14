package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFreightInvoiceSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFreightInvoiceSnapshotStore()
        val item = newScheduledFreightInvoiceSnapshot("code-982", "title-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
