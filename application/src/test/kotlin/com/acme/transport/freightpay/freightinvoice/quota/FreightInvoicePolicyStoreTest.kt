package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoicePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoicePolicyStore()
        val item = newFreightInvoicePolicy("code-517", "label-993")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
