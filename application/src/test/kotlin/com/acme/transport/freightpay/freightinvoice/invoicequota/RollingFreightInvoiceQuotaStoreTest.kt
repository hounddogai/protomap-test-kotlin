package com.acme.transport.freightpay.freightinvoice.invoicequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFreightInvoiceQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFreightInvoiceQuotaStore()
        val item = newRollingFreightInvoiceQuota("code-774", "name-850")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
