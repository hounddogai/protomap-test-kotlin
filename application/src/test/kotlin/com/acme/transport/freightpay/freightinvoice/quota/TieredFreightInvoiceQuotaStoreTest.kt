package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFreightInvoiceQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFreightInvoiceQuotaStore()
        val item = newTieredFreightInvoiceQuota("code-359", "name-990")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
