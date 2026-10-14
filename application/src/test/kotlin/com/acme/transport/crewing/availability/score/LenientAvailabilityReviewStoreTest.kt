package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAvailabilityReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAvailabilityReviewStore()
        val item = newLenientAvailabilityReview("code-112", "label-778")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
