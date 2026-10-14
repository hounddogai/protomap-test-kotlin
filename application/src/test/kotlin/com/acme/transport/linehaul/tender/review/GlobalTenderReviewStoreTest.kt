package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTenderReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTenderReviewStore()
        val item = newGlobalTenderReview("code-393", "title-911")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
