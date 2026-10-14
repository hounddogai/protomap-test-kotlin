package com.acme.transport.freightpay.freightinvoice.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultFreightInvoiceBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultFreightInvoiceBatchStore()
        val item = newDefaultFreightInvoiceBatch("code-499", "name-775")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
