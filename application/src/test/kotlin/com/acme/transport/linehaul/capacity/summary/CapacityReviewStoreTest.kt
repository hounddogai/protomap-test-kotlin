package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityReviewStore()
        val item = newCapacityReview("code-179", "label-504")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
