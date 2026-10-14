package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingReviewStore()
        val item = newCoachingReview("code-716", "title-846")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
