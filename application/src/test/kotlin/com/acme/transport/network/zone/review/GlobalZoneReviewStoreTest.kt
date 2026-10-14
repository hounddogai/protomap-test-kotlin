package com.acme.transport.network.zone.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalZoneReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalZoneReviewStore()
        val item = newGlobalZoneReview("code-846", "title-116")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
