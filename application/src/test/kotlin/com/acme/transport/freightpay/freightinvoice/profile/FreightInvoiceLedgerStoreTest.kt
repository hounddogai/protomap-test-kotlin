package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceLedgerStore()
        val item = newFreightInvoiceLedger("code-11", "name-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
