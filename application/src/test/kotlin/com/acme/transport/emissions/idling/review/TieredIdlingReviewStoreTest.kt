package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredIdlingReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredIdlingReviewStore()
        val item = newTieredIdlingReview("code-380", "name-422")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
