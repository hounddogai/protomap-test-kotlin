package com.acme.transport.freightpay.freightinvoice.invoicerollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalFreightInvoiceRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalFreightInvoiceRollupStore()
        val item = newGlobalFreightInvoiceRollup("code-168", "name-892")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
