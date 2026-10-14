package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedFreightInvoiceReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedFreightInvoiceReviewStore()
        val item = newBatchedFreightInvoiceReview("code-599", "title-553")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
