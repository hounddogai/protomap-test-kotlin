package com.acme.transport.freightpay.freightinvoice.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalFreightInvoiceThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalFreightInvoiceThresholdStore()
        val item = newRegionalFreightInvoiceThreshold("code-279", "title-347")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
