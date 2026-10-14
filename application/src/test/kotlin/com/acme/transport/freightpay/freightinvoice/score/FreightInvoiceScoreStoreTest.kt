package com.acme.transport.freightpay.freightinvoice.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceScoreStore()
        val item = newFreightInvoiceScore("code-910", "name-437")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
