package com.acme.transport.freightpay.freightinvoice.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceDigestStore()
        val item = newFreightInvoiceDigest("code-663", "label-487")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
