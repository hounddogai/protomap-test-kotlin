package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedScorecardReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedScorecardReviewStore()
        val item = newWeightedScorecardReview("code-649", "name-845")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
