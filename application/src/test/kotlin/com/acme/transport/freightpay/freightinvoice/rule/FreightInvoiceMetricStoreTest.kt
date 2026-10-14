package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceMetricStore()
        val item = newFreightInvoiceMetric("code-246", "name-379")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
