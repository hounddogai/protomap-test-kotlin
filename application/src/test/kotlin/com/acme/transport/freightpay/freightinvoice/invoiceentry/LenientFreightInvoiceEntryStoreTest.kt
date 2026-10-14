package com.acme.transport.freightpay.freightinvoice.invoiceentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientFreightInvoiceEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientFreightInvoiceEntryStore()
        val item = newLenientFreightInvoiceEntry("code-658", "name-851")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
