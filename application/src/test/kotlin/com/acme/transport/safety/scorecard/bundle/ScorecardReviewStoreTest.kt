package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardReviewStore()
        val item = newScorecardReview("code-105", "title-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
