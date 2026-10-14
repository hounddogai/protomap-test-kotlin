package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceSummaryStore()
        val item = newFreightInvoiceSummary("code-602", "title-344")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
