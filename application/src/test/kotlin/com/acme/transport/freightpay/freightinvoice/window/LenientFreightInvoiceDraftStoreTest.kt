package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientFreightInvoiceDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientFreightInvoiceDraftStore()
        val item = newLenientFreightInvoiceDraft("code-2", "label-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
