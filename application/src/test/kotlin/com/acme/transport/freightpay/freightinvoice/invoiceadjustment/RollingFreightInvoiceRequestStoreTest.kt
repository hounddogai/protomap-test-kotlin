package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFreightInvoiceRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFreightInvoiceRequestStore()
        val item = newRollingFreightInvoiceRequest("code-855", "title-429")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
