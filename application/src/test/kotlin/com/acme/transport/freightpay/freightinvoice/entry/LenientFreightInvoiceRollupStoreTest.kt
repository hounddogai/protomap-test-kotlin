package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientFreightInvoiceRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientFreightInvoiceRollupStore()
        val item = newLenientFreightInvoiceRollup("code-454", "label-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
