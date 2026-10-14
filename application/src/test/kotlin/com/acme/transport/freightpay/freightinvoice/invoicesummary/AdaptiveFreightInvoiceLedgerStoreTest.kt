package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveFreightInvoiceLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveFreightInvoiceLedgerStore()
        val item = newAdaptiveFreightInvoiceLedger("code-270", "title-579")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
