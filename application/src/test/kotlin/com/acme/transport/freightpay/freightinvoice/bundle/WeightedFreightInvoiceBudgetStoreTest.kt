package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedFreightInvoiceBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedFreightInvoiceBudgetStore()
        val item = newWeightedFreightInvoiceBudget("code-169", "title-633")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
