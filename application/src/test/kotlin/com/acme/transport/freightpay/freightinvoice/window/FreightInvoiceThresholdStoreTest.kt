package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceThresholdStore()
        val item = newFreightInvoiceThreshold("code-298", "label-544")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
