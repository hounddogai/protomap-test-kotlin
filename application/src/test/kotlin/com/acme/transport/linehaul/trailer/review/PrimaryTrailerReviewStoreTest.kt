package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTrailerReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTrailerReviewStore()
        val item = newPrimaryTrailerReview("code-646", "label-181")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
