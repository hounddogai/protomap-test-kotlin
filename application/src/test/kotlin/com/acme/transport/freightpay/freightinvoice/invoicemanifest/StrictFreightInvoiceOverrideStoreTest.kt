package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictFreightInvoiceOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictFreightInvoiceOverrideStore()
        val item = newStrictFreightInvoiceOverride("code-954", "name-873")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
