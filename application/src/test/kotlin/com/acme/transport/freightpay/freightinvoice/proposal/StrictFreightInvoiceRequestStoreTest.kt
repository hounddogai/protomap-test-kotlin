package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictFreightInvoiceRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictFreightInvoiceRequestStore()
        val item = newStrictFreightInvoiceRequest("code-216", "name-859")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
