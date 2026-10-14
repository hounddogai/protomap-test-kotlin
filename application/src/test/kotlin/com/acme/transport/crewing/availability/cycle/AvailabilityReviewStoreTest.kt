package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityReviewStore()
        val item = newAvailabilityReview("code-332", "name-317")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
