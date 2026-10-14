package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeReviewStore()
        val item = newTransitTimeReview("code-526", "label-575")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
