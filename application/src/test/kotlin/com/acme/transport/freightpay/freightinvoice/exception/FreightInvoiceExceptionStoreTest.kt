package com.acme.transport.freightpay.freightinvoice.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceExceptionStore()
        val item = newFreightInvoiceException("code-604", "title-597")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
