package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictFreightInvoiceScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictFreightInvoiceScoreStore()
        val item = newStrictFreightInvoiceScore("code-499", "title-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
