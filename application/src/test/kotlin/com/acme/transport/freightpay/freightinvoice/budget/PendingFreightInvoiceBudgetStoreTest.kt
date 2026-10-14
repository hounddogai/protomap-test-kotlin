package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingFreightInvoiceBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingFreightInvoiceBudgetStore()
        val item = newPendingFreightInvoiceBudget("code-286", "title-838")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
