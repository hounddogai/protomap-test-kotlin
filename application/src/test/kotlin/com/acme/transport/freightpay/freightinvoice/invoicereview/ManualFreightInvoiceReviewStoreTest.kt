package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualFreightInvoiceReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualFreightInvoiceReviewStore()
        val item = newManualFreightInvoiceReview("code-623", "name-999")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
