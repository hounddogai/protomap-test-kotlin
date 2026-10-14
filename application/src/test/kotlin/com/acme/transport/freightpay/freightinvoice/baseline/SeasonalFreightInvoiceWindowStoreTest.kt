package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalFreightInvoiceWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalFreightInvoiceWindowStore()
        val item = newSeasonalFreightInvoiceWindow("code-897", "name-255")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
