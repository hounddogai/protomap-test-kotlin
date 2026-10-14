package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAvailabilityReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAvailabilityReviewStore()
        val item = newPendingAvailabilityReview("code-193", "label-952")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
