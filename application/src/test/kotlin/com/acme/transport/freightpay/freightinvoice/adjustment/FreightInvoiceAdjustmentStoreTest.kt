package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceAdjustmentStore()
        val item = newFreightInvoiceAdjustment("code-384", "name-598")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
