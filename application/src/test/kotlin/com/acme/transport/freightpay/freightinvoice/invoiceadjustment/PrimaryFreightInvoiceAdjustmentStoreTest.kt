package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryFreightInvoiceAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryFreightInvoiceAdjustmentStore()
        val item = newPrimaryFreightInvoiceAdjustment("code-923", "label-420")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
